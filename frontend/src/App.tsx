import { useEffect, useRef, useState } from 'react'
import { apiDelete, apiGet, apiPatch, apiPost, apiPostForm, apiPostSse } from './api/client'
import type {
  AgentSession,
  ChatRequest,
  ChatResponse,
  ConversationMessage,
  CreateMessageRequest,
  CreateSessionRequest,
  DocumentEmbedding,
  KnowledgeBase,
  IngestFolderRequest,
  ModelChannelHealth,
  ObservabilitySummary
} from './api/types'

type SidebarPanel = 'chat' | 'mcp' | 'monitoring' | 'knowledge'
type McpTransport = 'stdio' | 'sse' | 'http'

interface McpServerConfig {
  name: string
  transport: McpTransport
  endpoint: string
  enabled: boolean
}

const DEFAULT_MCP_CONFIGS: McpServerConfig[] = []

function App() {
  const [sessions, setSessions] = useState<AgentSession[]>([])
  const [selectedSessionId, setSelectedSessionId] = useState('')
  const [messages, setMessages] = useState<ConversationMessage[]>([])
  const [draft, setDraft] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [activePanel, setActivePanel] = useState<SidebarPanel>('chat')
  const [mcpConfigs, setMcpConfigs] = useState<McpServerConfig[]>(() => loadMcpConfigs())
  const [monitoringLoading, setMonitoringLoading] = useState(false)
  const [monitoringSummary, setMonitoringSummary] = useState<ObservabilitySummary | null>(null)
  const [modelChannels, setModelChannels] = useState<ModelChannelHealth | null>(null)
  const [knowledgeBusy, setKnowledgeBusy] = useState(false)
  const [knowledgeBases, setKnowledgeBases] = useState<KnowledgeBase[]>([])
  const [selectedKnowledgeBaseId, setSelectedKnowledgeBaseId] = useState('')
  const [newKnowledgeBaseName, setNewKnowledgeBaseName] = useState('')
  const [newKnowledgeBaseDescription, setNewKnowledgeBaseDescription] = useState('')
  const [knowledgeDocuments, setKnowledgeDocuments] = useState<DocumentEmbedding[]>([])
  const [selectedFiles, setSelectedFiles] = useState<File[]>([])
  const [folderPath, setFolderPath] = useState('')
  const [recursiveFolder, setRecursiveFolder] = useState(true)
  const [dragActive, setDragActive] = useState(false)
  const [ingestedDocuments, setIngestedDocuments] = useState<DocumentEmbedding[]>([])
  const [chatKnowledgeBaseIds, setChatKnowledgeBaseIds] = useState<string[]>([])
  const [showKbPicker, setShowKbPicker] = useState(false)
  const draftRef = useRef<HTMLTextAreaElement | null>(null)

  function loadMcpConfigs() {
    const raw = window.localStorage.getItem('chlobot:mcp-configs')
    if (!raw) return DEFAULT_MCP_CONFIGS
    try {
      const parsed = JSON.parse(raw)
      return Array.isArray(parsed) ? parsed as McpServerConfig[] : DEFAULT_MCP_CONFIGS
    } catch {
      return DEFAULT_MCP_CONFIGS
    }
  }

  function saveMcpConfigs(next: McpServerConfig[]) {
    setMcpConfigs(next)
    window.localStorage.setItem('chlobot:mcp-configs', JSON.stringify(next))
  }

  async function refreshSessions(selectFirst = false) {
    const data = await apiGet<AgentSession[]>('/sessions')
    setSessions(data)
    if (selectFirst && !selectedSessionId && data[0]) {
      setSelectedSessionId(data[0].id)
    }
  }

  async function loadMessages(sessionId: string) {
    if (!sessionId) return
    setMessages(await apiGet<ConversationMessage[]>(`/sessions/${sessionId}/messages`))
  }

  async function newChat() {
    setError('')
    setActivePanel('chat')
    const payload: CreateSessionRequest = {
      title: '新对话',
      metadata: { source: 'chat-ui' }
    }
    const session = await apiPost<AgentSession>('/sessions', payload)
    setSessions([session, ...sessions])
    setSelectedSessionId(session.id)
    setMessages([])
    setDraft('')
  }

  async function deleteSession(sessionId: string) {
    const session = sessions.find(item => item.id === sessionId)
    const confirmed = window.confirm(`删除会话「${session?.title || '未命名对话'}」？该操作会移除当前聊天记录。`)
    if (!confirmed) return
    setError('')
    try {
      await apiDelete(`/sessions/${sessionId}`)
      const nextSessions = sessions.filter(item => item.id !== sessionId)
      setSessions(nextSessions)
      if (selectedSessionId === sessionId) {
        const nextSession = nextSessions[0]
        setSelectedSessionId(nextSession?.id || '')
        if (!nextSession) {
          setMessages([])
        }
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : '删除会话失败')
    }
  }

  async function sendMessage() {
    const content = draft.trim()
    if (!content || busy) return
    setBusy(true)
    setError('')
    setActivePanel('chat')
    try {
      let sessionId = selectedSessionId
      if (!sessionId) {
        const session = await apiPost<AgentSession>('/sessions', {
          title: content.slice(0, 28) || '新对话',
          metadata: { source: 'chat-ui' }
        } as CreateSessionRequest)
        sessionId = session.id
        setSelectedSessionId(session.id)
        setSessions([session, ...sessions])
      }

      const userMessage: CreateMessageRequest = { role: 'USER', content, metadata: {} }
      await apiPost(`/sessions/${sessionId}/messages`, userMessage)
      setMessages(previous => [...previous, {
        id: Date.now(),
        sessionId,
        role: 'USER',
        content,
        metadata: {},
        createdAt: new Date().toISOString()
      }])
      setDraft('')

      const assistantTempId = Date.now() + 1
      let assistantContent = ''
      let assistantMeta: Pick<ChatResponse, 'provider' | 'model' | 'mock'> = {
        provider: '',
        model: '',
        mock: false
      }

      setMessages(previous => [...previous, {
        id: assistantTempId,
        sessionId,
        role: 'ASSISTANT',
        content: '',
        metadata: {},
        createdAt: new Date().toISOString()
      }])

      await apiPostSse<ChatResponse>('/chat/stream', {
        message: content,
        system: 'You are Chlobot, a helpful user-facing assistant.',
        knowledgeBaseIds: chatKnowledgeBaseIds.length ? chatKnowledgeBaseIds : undefined
      } as ChatRequest, chunk => {
        assistantContent += chunk.content ?? ''
        assistantMeta = { provider: chunk.provider, model: chunk.model, mock: chunk.mock }

        setMessages(previous => previous.map(message =>
          message.id === assistantTempId
            ? {
                ...message,
                content: assistantContent,
                metadata: assistantMeta
              }
            : message
        ))
      })

      const assistantPayload: CreateMessageRequest = {
        role: 'ASSISTANT',
        content: assistantContent || '（模型没有返回内容）',
        metadata: assistantMeta
      }
      await apiPost(`/sessions/${sessionId}/messages`, assistantPayload)
      await loadMessages(sessionId)
      await refreshSessions()
    } catch (err) {
      setError(err instanceof Error ? err.message : '发送失败')
    } finally {
      setBusy(false)
    }
  }

  async function loadMonitoring() {
    setMonitoringLoading(true)
    setError('')
    try {
      const [summary, channels] = await Promise.all([
        apiGet<ObservabilitySummary>('/observability/metrics/summary'),
        apiGet<ModelChannelHealth>('/model/channels')
      ])
      setMonitoringSummary(summary)
      setModelChannels(channels)
    } catch (err) {
      setError(err instanceof Error ? err.message : '监控数据加载失败')
    } finally {
      setMonitoringLoading(false)
    }
  }

  async function loadKnowledgeBases(selectFirst = false) {
    const data = await apiGet<KnowledgeBase[]>('/rag/knowledge-bases')
    setKnowledgeBases(data)
    if ((!selectedKnowledgeBaseId || selectFirst) && data[0]) {
      setSelectedKnowledgeBaseId(data[0].id)
    }
  }

  async function loadKnowledgeDocuments(knowledgeBaseId: string) {
    if (!knowledgeBaseId) {
      setKnowledgeDocuments([])
      return
    }
    setKnowledgeDocuments(await apiGet<DocumentEmbedding[]>(`/rag/knowledge-bases/${knowledgeBaseId}/documents`))
  }

  async function createKnowledgeBase() {
    if (!newKnowledgeBaseName.trim()) return
    setKnowledgeBusy(true)
    setError('')
    try {
      const created = await apiPost<KnowledgeBase>('/rag/knowledge-bases', {
        name: newKnowledgeBaseName.trim(),
        description: newKnowledgeBaseDescription.trim(),
        enabled: true
      })
      setKnowledgeBases([created, ...knowledgeBases])
      setSelectedKnowledgeBaseId(created.id)
      setNewKnowledgeBaseName('')
      setNewKnowledgeBaseDescription('')
    } catch (err) {
      setError(err instanceof Error ? err.message : '创建知识库失败')
    } finally {
      setKnowledgeBusy(false)
    }
  }

  async function toggleKnowledgeBase(base: KnowledgeBase) {
    setKnowledgeBusy(true)
    setError('')
    try {
      const updated = await apiPatch<KnowledgeBase>(`/rag/knowledge-bases/${base.id}`, { enabled: !base.enabled })
      setKnowledgeBases(previous => previous.map(item => item.id === updated.id ? updated : item))
    } catch (err) {
      setError(err instanceof Error ? err.message : '更新知识库失败')
    } finally {
      setKnowledgeBusy(false)
    }
  }

  async function renameKnowledgeBase(base: KnowledgeBase, name: string) {
    const nextName = name.trim()
    if (!nextName || nextName === base.name) return
    setKnowledgeBusy(true)
    setError('')
    try {
      const updated = await apiPatch<KnowledgeBase>(`/rag/knowledge-bases/${base.id}`, { name: nextName })
      setKnowledgeBases(previous => previous.map(item => item.id === updated.id ? updated : item))
    } catch (err) {
      setError(err instanceof Error ? err.message : '重命名知识库失败')
    } finally {
      setKnowledgeBusy(false)
    }
  }

  async function deleteKnowledgeBase(base: KnowledgeBase) {
    const confirmed = window.confirm(`删除知识库「${base.name}」？该知识库下的文档也会被移除。`)
    if (!confirmed) return
    setKnowledgeBusy(true)
    setError('')
    try {
      await apiDelete(`/rag/knowledge-bases/${base.id}`)
      const nextBases = knowledgeBases.filter(item => item.id !== base.id)
      setKnowledgeBases(nextBases)
      if (selectedKnowledgeBaseId === base.id) {
        setSelectedKnowledgeBaseId(nextBases[0]?.id || '')
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : '删除知识库失败')
    } finally {
      setKnowledgeBusy(false)
    }
  }

  async function deleteKnowledgeDocument(document: DocumentEmbedding) {
    const confirmed = window.confirm(`删除文档「${document.title}」？`)
    if (!confirmed) return
    setKnowledgeBusy(true)
    setError('')
    try {
      await apiDelete(`/rag/documents/${document.documentId}`)
      setKnowledgeDocuments(previous => previous.filter(item => item.documentId !== document.documentId))
      setIngestedDocuments(previous => previous.filter(item => item.documentId !== document.documentId))
    } catch (err) {
      setError(err instanceof Error ? err.message : '删除文档失败')
    } finally {
      setKnowledgeBusy(false)
    }
  }

  async function ingestFiles() {
    if (!selectedFiles.length || !selectedKnowledgeBaseId) return
    setKnowledgeBusy(true)
    setError('')
    try {
      const form = new FormData()
      selectedFiles.forEach(file => form.append('files', file))
      const results = await apiPostForm<DocumentEmbedding[]>(`/rag/documents/files?knowledgeBaseId=${encodeURIComponent(selectedKnowledgeBaseId)}`, form)
      setIngestedDocuments(results)
      await loadKnowledgeDocuments(selectedKnowledgeBaseId)
      setSelectedFiles([])
    } catch (err) {
      setError(err instanceof Error ? err.message : '文件导入失败')
    } finally {
      setKnowledgeBusy(false)
    }
  }

  async function ingestFolder() {
    if (!folderPath.trim() || !selectedKnowledgeBaseId) return
    setKnowledgeBusy(true)
    setError('')
    try {
      const payload: IngestFolderRequest = {
        folderPath: folderPath.trim(),
        recursive: recursiveFolder,
        metadata: { source: 'frontend-folder-import', knowledgeBaseId: selectedKnowledgeBaseId },
        knowledgeBaseId: selectedKnowledgeBaseId
      }
      const results = await apiPost<DocumentEmbedding[]>('/rag/documents/folder', payload)
      setIngestedDocuments(results)
      await loadKnowledgeDocuments(selectedKnowledgeBaseId)
      setFolderPath('')
    } catch (err) {
      setError(err instanceof Error ? err.message : '文件夹导入失败')
    } finally {
      setKnowledgeBusy(false)
    }
  }

  function addDroppedFiles(fileList: FileList | null) {
    if (!fileList?.length) return
    const nextFiles = Array.from(fileList)
    setSelectedFiles(previous => {
      const existing = new Set(previous.map(file => `${file.name}-${file.size}-${file.lastModified}`))
      return [...previous, ...nextFiles.filter(file => !existing.has(`${file.name}-${file.size}-${file.lastModified}`))]
    })
  }

  useEffect(() => {
    void refreshSessions(true)
  }, [])

  useEffect(() => {
    void loadKnowledgeBases(true)
  }, [])

  useEffect(() => {
    void loadMessages(selectedSessionId)
  }, [selectedSessionId])

  useEffect(() => {
    void loadKnowledgeDocuments(selectedKnowledgeBaseId)
  }, [selectedKnowledgeBaseId])

  useEffect(() => {
    const textarea = draftRef.current
    if (!textarea) return
    textarea.style.height = 'auto'
    textarea.style.height = `${Math.min(textarea.scrollHeight, 176)}px`
  }, [draft])

  useEffect(() => {
    if (activePanel === 'monitoring') {
      void loadMonitoring()
    }
  }, [activePanel])

  const activeSession = sessions.find(session => session.id === selectedSessionId)
  const hasConversation = messages.length > 0

  function toggleChatKb(id: string) {
    setChatKnowledgeBaseIds(previous =>
      previous.includes(id) ? previous.filter(item => item !== id) : [...previous, id]
    )
  }

  const kbPicker = knowledgeBases.length ? (
    <div className="kb-picker">
      <button className="kb-picker-toggle" type="button" onClick={() => setShowKbPicker(!showKbPicker)}>
        {chatKnowledgeBaseIds.length ? `已选 ${chatKnowledgeBaseIds.length} 个知识库` : '选择知识库'}
      </button>
      {showKbPicker ? (
        <div className="kb-picker-dropdown">
          {knowledgeBases.filter(base => base.enabled).map(base => (
            <label key={base.id} className="check-row">
              <input type="checkbox" checked={chatKnowledgeBaseIds.includes(base.id)} onChange={() => toggleChatKb(base.id)} />
              {base.name}
            </label>
          ))}
          {!knowledgeBases.filter(base => base.enabled).length ? <p className="empty-hint">暂无已启用的知识库</p> : null}
        </div>
      ) : null}
    </div>
  ) : null

  const composer = (
    <form className="composer" onSubmit={event => { event.preventDefault(); void sendMessage() }}>
      {kbPicker}
      <div className="composer-input">
        <textarea ref={draftRef} value={draft} placeholder="发送消息给 Chlobot" rows={1} onChange={event => setDraft(event.target.value)} onKeyDown={event => {
          if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault()
            void sendMessage()
          }
        }} />
        <button type="submit" className="send-button" aria-label="发送消息" disabled={busy || !draft.trim()}>
          <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
            <path d="M3 11.5L20.5 4l-7.5 17.5-2.5-7-7.5-3z" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinejoin="round" strokeLinecap="round" />
            <path d="M10.5 14.5L20.5 4" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinejoin="round" strokeLinecap="round" />
          </svg>
        </button>
      </div>
    </form>
  )

  const mainContent = activePanel === 'chat' ? (
    <main className="chat-main">
      {hasConversation ? (
        <header className="chat-titlebar">
          <div>
            <h2>{activeSession?.title || '新对话'}</h2>
            <p>和 Chlobot 直接对话，历史会话会自动保存。</p>
          </div>
        </header>
      ) : null}

      <section className="message-list">
        {!hasConversation ? (
          <div className="welcome">
            <h2>今天想让 Chlobot 帮你做什么？</h2>
            <p>可以直接输入问题、需求、研究主题或项目任务。</p>
            <div className="welcome-chatbox">
              {composer}
            </div>
            <div className="prompt-examples">
              <button onClick={() => setDraft('帮我规划一个 Java Spring AI Agent 功能')}>规划功能</button>
              <button onClick={() => setDraft('总结当前会话，并给我下一步建议')}>总结建议</button>
              <button onClick={() => setDraft('帮我写一个 RAG 检索问题')}>RAG 问题</button>
            </div>
          </div>
        ) : messages.map(message => (
          <article key={message.id} className={`message ${message.role.toLowerCase()}`}>
            <div className="avatar">{message.role === 'USER' ? '你' : 'C'}</div>
            <div className="message-body">
              <strong>{message.role === 'USER' ? '你' : 'Chlobot'}</strong>
              <p>{message.content || '正在回复...'}</p>
            </div>
          </article>
        ))}
      </section>

      <footer className="chat-footer">
        {error ? <div className="chat-error">{error}</div> : null}
        {hasConversation ? composer : null}
      </footer>
    </main>
  ) : (
    <main className="settings-main">
      {error ? <div className="chat-error settings-error">{error}</div> : null}
      {activePanel === 'mcp' ? renderMcpPanel() : null}
      {activePanel === 'monitoring' ? renderMonitoringPanel() : null}
      {activePanel === 'knowledge' ? renderKnowledgePanel() : null}
    </main>
  )

  function iconButton(panel: SidebarPanel, label: string, icon: 'mcp' | 'monitoring' | 'knowledge') {
    return (
      <button className={activePanel === panel ? 'sidebar-icon active' : 'sidebar-icon'} aria-label={label} title={label} onClick={() => setActivePanel(panel)}>
        {renderIcon(icon)}
      </button>
    )
  }

  function renderMcpPanel() {
    return (
      <section className="settings-panel">
        <header className="settings-header">
          <div>
            <p className="eyebrow">MCP</p>
            <h2>MCP 配置</h2>
            <p>管理工具服务连接。密钥仍应放在后端 `.secrets/`，前端只保存非敏感连接草稿。</p>
          </div>
        </header>
        <div className="settings-grid">
          {mcpConfigs.map((config, index) => (
            <article className="settings-card" key={`${config.name}-${index}`}>
              <label>服务名称
                <input value={config.name} onChange={event => {
                  const next = [...mcpConfigs]
                  next[index] = { ...config, name: event.target.value }
                  saveMcpConfigs(next)
                }} />
              </label>
              <label>传输方式
                <select value={config.transport} onChange={event => {
                  const next = [...mcpConfigs]
                  next[index] = { ...config, transport: event.target.value as McpTransport }
                  saveMcpConfigs(next)
                }}>
                  <option value="stdio">stdio</option>
                  <option value="sse">SSE</option>
                  <option value="http">HTTP</option>
                </select>
              </label>
              <label>命令或地址
                <input value={config.endpoint} onChange={event => {
                  const next = [...mcpConfigs]
                  next[index] = { ...config, endpoint: event.target.value }
                  saveMcpConfigs(next)
                }} />
              </label>
              <label className="check-row">
                <input type="checkbox" checked={config.enabled} onChange={event => {
                  const next = [...mcpConfigs]
                  next[index] = { ...config, enabled: event.target.checked }
                  saveMcpConfigs(next)
                }} />
                启用此 MCP 服务
              </label>
            </article>
          ))}
        </div>
        <button className="secondary-action" onClick={() => saveMcpConfigs([...mcpConfigs, { name: '新 MCP 服务', transport: 'sse', endpoint: '', enabled: false }])}>添加 MCP 服务</button>
      </section>
    )
  }

  function renderMonitoringPanel() {
    return (
      <section className="settings-panel">
        <header className="settings-header">
          <div>
            <p className="eyebrow">Monitor</p>
            <h2>监控</h2>
            <p>查看会话、模型、RAG 与工具调用的运行状态。</p>
          </div>
          <button className="secondary-action" onClick={() => void loadMonitoring()} disabled={monitoringLoading}>{monitoringLoading ? '刷新中' : '刷新'}</button>
        </header>
        <div className="metric-grid">
          <MetricCard label="会话" value={monitoringSummary?.sessionCount ?? '-'} />
          <MetricCard label="任务" value={monitoringSummary?.taskCount ?? '-'} />
          <MetricCard label="模型调用" value={monitoringSummary?.model.calls ?? '-'} />
          <MetricCard label="Fallback" value={monitoringSummary?.model.fallbacks ?? '-'} />
        </div>
        <div className="settings-grid">
          <article className="settings-card">
            <h3>模型渠道</h3>
            <p>当前渠道：{modelChannels?.active || '-'}</p>
            <p>Fallback：{modelChannels ? (modelChannels.fallbackEnabled ? '已启用' : '已关闭') : '-'}</p>
            <div className="tag-list">
              {modelChannels?.channels.map(channel => <span key={channel.name}>{channel.provider}/{channel.model}</span>)}
            </div>
          </article>
          <article className="settings-card">
            <h3>RAG 指标</h3>
            <p>Hit@5：{monitoringSummary?.rag.hitAt5 ?? '-'}</p>
            <p>Recall@5：{monitoringSummary?.rag.recallAt5 ?? '-'}</p>
            <p>MRR：{monitoringSummary?.rag.mrr ?? '-'}</p>
          </article>
          <article className="settings-card">
            <h3>Prometheus</h3>
            <p>后端已开放 Prometheus 指标端点。</p>
            <a href="/api/actuator/prometheus" target="_blank" rel="noreferrer">打开 /api/actuator/prometheus</a>
          </article>
        </div>
      </section>
    )
  }

  function renderKnowledgePanel() {
    const selectedBase = knowledgeBases.find(base => base.id === selectedKnowledgeBaseId)
    return (
      <section className="settings-panel">
        <header className="settings-header">
          <div>
            <p className="eyebrow">Knowledge</p>
            <h2>知识库管理</h2>
            <p>创建知识库、导入文档，管理已有知识库和文档。知识库供 Bot 对话时使用。</p>
          </div>
        </header>
        <div className="settings-grid two-column">
          <article className="settings-card wide-card">
            <h3>创建知识库 / 导入文档</h3>
            <div className="knowledge-create">
              <label>知识库名称<input value={newKnowledgeBaseName} onChange={event => setNewKnowledgeBaseName(event.target.value)} placeholder="例如：项目文档" /></label>
              <label>描述<input value={newKnowledgeBaseDescription} onChange={event => setNewKnowledgeBaseDescription(event.target.value)} placeholder="这个知识库收录什么资料" /></label>
              <button className="primary-action" disabled={knowledgeBusy || !newKnowledgeBaseName.trim()} onClick={() => void createKnowledgeBase()}>创建知识库</button>
            </div>
            {knowledgeBases.length ? (
              <div className="folder-import">
                <label>导入到
                  <select value={selectedKnowledgeBaseId} onChange={event => setSelectedKnowledgeBaseId(event.target.value)}>
                    {knowledgeBases.map(base => <option key={base.id} value={base.id}>{base.name}</option>)}
                  </select>
                </label>
              </div>
            ) : null}
            <div
              className={dragActive ? 'drop-zone drag-active' : 'drop-zone'}
              onDragEnter={event => { event.preventDefault(); setDragActive(true) }}
              onDragOver={event => { event.preventDefault(); setDragActive(true) }}
              onDragLeave={event => { event.preventDefault(); setDragActive(false) }}
              onDrop={event => {
                event.preventDefault()
                setDragActive(false)
                addDroppedFiles(event.dataTransfer.files)
              }}
            >
              <strong>拖拽文件到这里</strong>
              <p>支持 txt、md、json、csv、yaml、代码文件等文本资料。</p>
              <input type="file" multiple disabled={!selectedKnowledgeBaseId} onChange={event => addDroppedFiles(event.target.files)} />
            </div>
            {selectedFiles.length ? (
              <div className="file-list">
                {selectedFiles.map(file => (
                  <div className="file-item" key={`${file.name}-${file.size}-${file.lastModified}`}>
                    <span>{file.name}</span>
                    <small>{Math.ceil(file.size / 1024)} KB</small>
                  </div>
                ))}
              </div>
            ) : null}
            <div className="action-row">
              <button className="primary-action" disabled={knowledgeBusy || !selectedFiles.length || !selectedKnowledgeBaseId} onClick={() => void ingestFiles()}>导入选中文件</button>
              <button className="secondary-action" disabled={knowledgeBusy || !selectedFiles.length} onClick={() => setSelectedFiles([])}>清空文件</button>
            </div>
            <div className="folder-import">
              <label>服务器文件夹路径<input value={folderPath} disabled={!selectedKnowledgeBaseId} onChange={event => setFolderPath(event.target.value)} placeholder="例如 /data/docs 或 D:/docs" /></label>
              <label className="check-row">
                <input type="checkbox" checked={recursiveFolder} onChange={event => setRecursiveFolder(event.target.checked)} />
                递归导入子文件夹
              </label>
              <button className="secondary-action" disabled={knowledgeBusy || !folderPath.trim() || !selectedKnowledgeBaseId} onClick={() => void ingestFolder()}>按文件夹路径导入</button>
            </div>
            {ingestedDocuments.length ? <p className="success-text">已导入 {ingestedDocuments.length} 个文档：{ingestedDocuments.slice(0, 3).map(document => document.title).join('、')}</p> : null}
          </article>

          <article className="settings-card wide-card">
            <h3>文档管理</h3>
            <div className="knowledge-base-list">
              {knowledgeBases.map(base => (
                <div key={base.id} className={base.id === selectedKnowledgeBaseId ? 'knowledge-base-item active' : 'knowledge-base-item'}>
                  <button className="knowledge-base-select" onClick={() => setSelectedKnowledgeBaseId(base.id)}>
                    <strong>{base.name}</strong>
                    <small>{base.enabled ? '已启用' : '已停用'} · {base.description || '无描述'} · {knowledgeDocuments.length && base.id === selectedKnowledgeBaseId ? `${knowledgeDocuments.length} 篇文档` : ''}</small>
                  </button>
                  <div className="knowledge-base-actions">
                    <label className="check-row">
                      <input type="checkbox" checked={base.enabled} disabled={knowledgeBusy} onChange={() => void toggleKnowledgeBase(base)} />
                      启用
                    </label>
                    <button className="secondary-action" disabled={knowledgeBusy} onClick={() => {
                      const nextName = window.prompt('新的知识库名称', base.name)
                      if (nextName) void renameKnowledgeBase(base, nextName)
                    }}>改名</button>
                    <button className="danger-action" disabled={knowledgeBusy} onClick={() => void deleteKnowledgeBase(base)}>删除</button>
                  </div>
                </div>
              ))}
              {!knowledgeBases.length ? <p className="empty-hint">还没有知识库，请先创建一个。</p> : null}
            </div>
            {selectedKnowledgeBaseId ? (
              <div className="result-list">
                {knowledgeDocuments.map(document => (
                  <div className="result-item document-manager-item" key={document.documentId}>
                    <div>
                      <strong>{document.title}</strong>
                      <p>{document.content.slice(0, 200)}</p>
                    </div>
                    <button className="danger-action" disabled={knowledgeBusy} onClick={() => void deleteKnowledgeDocument(document)}>删除</button>
                  </div>
                ))}
                {!knowledgeDocuments.length ? <p className="empty-hint">当前知识库还没有文档。</p> : null}
              </div>
            ) : null}
          </article>
        </div>
      </section>
    )
  }

  return (
    <div className="chat-product">
      <aside className="conversation-sidebar">
        <div className="brand">
          <div className="brand-mark">C</div>
          <div>
            <h1>Chlobot</h1>
            <p>AI 对话助手</p>
          </div>
        </div>
        <button className="new-chat" onClick={() => void newChat()}>+ 新对话</button>
        <div className="sidebar-tools" aria-label="系统设置导航">
          {iconButton('mcp', 'MCP 配置', 'mcp')}
          {iconButton('monitoring', '监控', 'monitoring')}
          {iconButton('knowledge', '知识库数据导入与管理', 'knowledge')}
        </div>
        <div className="conversation-list">
          {sessions.map(session => (
            <div key={session.id} className={session.id === selectedSessionId ? 'conversation-row active' : 'conversation-row'}>
              <button className="conversation" onClick={() => { setActivePanel('chat'); setSelectedSessionId(session.id) }}>
                <span className="conversation-title">{session.title || '未命名对话'}</span>
                <small>{new Date(session.updatedAt || session.createdAt).toLocaleString()}</small>
              </button>
              <button className="delete-conversation" title="删除会话" aria-label={`删除会话 ${session.title || '未命名对话'}`} onClick={() => void deleteSession(session.id)}>×</button>
            </div>
          ))}
          {!sessions.length ? <p className="empty-hint">还没有对话，发送第一条消息即可开始。</p> : null}
        </div>
      </aside>
      {mainContent}
    </div>
  )
}

function MetricCard({ label, value }: { label: string; value: string | number }) {
  return (
    <article className="metric-card">
      <span>{label}</span>
      <strong>{value}</strong>
    </article>
  )
}

function renderIcon(icon: 'mcp' | 'monitoring' | 'knowledge') {
  if (icon === 'mcp') {
    return (
      <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
        <path d="M7 8h10M7 16h10M9 4v4M15 4v4M9 16v4M15 16v4" />
        <rect x="5" y="8" width="14" height="8" rx="3" />
      </svg>
    )
  }
  if (icon === 'monitoring') {
    return (
      <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
        <path d="M4 18V6" />
        <path d="M4 18h16" />
        <path d="M7 15l3-4 3 2 4-6" />
        <circle cx="17" cy="7" r="1" />
      </svg>
    )
  }
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
      <path d="M5 5.5A2.5 2.5 0 0 1 7.5 3H19v16H7.5A2.5 2.5 0 0 0 5 21.5z" />
      <path d="M5 5.5v16" />
      <path d="M9 7h6M9 11h7" />
    </svg>
  )
}

export default App
