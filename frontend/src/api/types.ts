export type JsonMap = Record<string, unknown>

export type TaskStatus =
  | 'PENDING'
  | 'ANALYZING'
  | 'PENDING_CLARIFICATION'
  | 'PLANNING'
  | 'PENDING_CONFIRMATION'
  | 'CONFIRMED'
  | 'EXECUTING'
  | 'WAITING_USER_INPUT'
  | 'COMPLETED'
  | 'FAILED'
  | 'CANCELLED'

export type NodeStatus = 'PENDING' | 'READY' | 'CLAIMED' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'SKIPPED'
export type GraphStatus = 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED'

export interface AgentSession {
  id: string
  title: string
  metadata: JsonMap
  createdAt: string
  updatedAt: string
}

export interface CreateSessionRequest {
  title?: string
  metadata?: JsonMap
}

export interface ConversationMessage {
  id: number
  sessionId: string
  role: 'USER' | 'ASSISTANT' | 'SYSTEM' | 'TOOL'
  content: string
  metadata: JsonMap
  createdAt: string
}

export interface CreateMessageRequest {
  role: string
  content: string
  metadata?: JsonMap
}

export interface AgentTask {
  id: string
  sessionId: string
  input: string
  mode?: string
  status: TaskStatus
  riskLevel?: string
  budget: JsonMap
  result: JsonMap
  errorCode?: string
  errorMessage?: string
  createdAt: string
  updatedAt: string
}

export interface CreateAgentTaskRequest {
  sessionId: string
  input: string
  mode?: string
  requiresPlanConfirmation?: boolean
  budget?: JsonMap
}

export interface CreateAgentTaskResponse {
  taskId: string
  status: TaskStatus
  streamUrl: string
  task: JsonMap
  createdAt: string
}

export interface AgentTaskEvent {
  id?: number
  taskId: string
  eventType: string
  sequenceNo: number
  payload: JsonMap
  createdAt: string
}

export interface ChatRequest {
  message: string
  system?: string
  knowledgeBaseIds?: string[]
}

export interface ChatResponse {
  content: string
  provider: string
  model: string
  mock: boolean
  intent?: { intent: string; summary: string } | null
}

export interface ModelChannel {
  name: string
  provider: string
  model: string
  resourceName: string
  priority: number
  fallback: boolean
}

export interface ModelChannelHealth {
  active: string
  retryPolicy: string
  fallbackEnabled: boolean
  sentinelResource: string
  channels: ModelChannel[]
}

export interface AgentMemory {
  id?: string
  memoryType: string
  content: string
  source?: string
  confidence?: number
  metadata?: JsonMap
  createdAt?: string
  updatedAt?: string
  deleted?: boolean
}

export interface MemoryPromptContext {
  memories: AgentMemory[]
  systemPromptSection: string
  budgetTokens: number
  usedTokens: number
  droppedMemories: number
  metadata: JsonMap
}

export interface KnowledgeBase {
  id: string
  name: string
  description?: string | null
  enabled: boolean
  createdAt: string
  updatedAt: string
}

export interface CreateKnowledgeBaseRequest {
  name: string
  description?: string
  enabled?: boolean
}

export interface UpdateKnowledgeBaseRequest {
  name?: string
  description?: string
  enabled?: boolean
}

export interface IngestDocumentRequest {
  title: string
  content: string
  sourceUri?: string
  metadata?: JsonMap
  knowledgeBaseId?: string
}

export interface IngestFolderRequest {
  folderPath: string
  recursive?: boolean
  metadata?: JsonMap
  knowledgeBaseId?: string
}

export interface DocumentEmbedding {
  id: number
  documentId: string
  chunkId: string
  title: string
  content: string
  embedding: unknown
  metadata: JsonMap
  createdAt: string
  updatedAt: string
  knowledgeBaseId?: string | null
  enabled: boolean
}

export interface RagSearchRequest {
  query: string
  topK?: number
  mode?: string
  knowledgeBaseId?: string
}

export interface RagSearchResult {
  documentId: string
  chunkId: string
  title: string
  content: string
  score: number
  metadata: JsonMap
}

export interface GraphWriteRequest {
  taskId: string
  nodes: NodeSpec[]
  edges: EdgeSpec[]
}

export interface NodeSpec {
  id: string
  name: string
  nodeType: string
  priority?: number
  payload?: JsonMap
}

export interface EdgeSpec {
  fromNodeId: string
  toNodeId: string
  edgeType?: string
}

export interface AgentTaskNode {
  id: string
  taskId: string
  name: string
  nodeType: string
  status: NodeStatus
  owner?: string
  priority: number
  payload: JsonMap
  result: JsonMap
  errorMessage?: string
  createdAt: string
  updatedAt: string
}

export interface AgentTaskEdge {
  id: number
  taskId: string
  fromNodeId: string
  toNodeId: string
  edgeType: string
  createdAt: string
}

export interface AgentTaskGraph {
  taskId: string
  status: GraphStatus
  nodes: AgentTaskNode[]
  edges: AgentTaskEdge[]
  parallelGroups: string[][]
}

export interface ObservabilitySummary {
  taskCount: number
  sessionCount: number
  status: 'ok'
  model: { calls: number; retries: number; fallbacks: number; estimatedTokens: number }
  rag: { hitAt5: number; recallAt5: number; mrr: number; citationCoverage: number; emptyRetrievalRate: number }
  tools: { pendingConfirmations: number; approvals: number; rejections: number; riskyCalls: number }
  memory: { confirmedFacts: number; selected: number; dropped: number; ruleConflicts: number }
  contextBudget: { windowTokens: number; outputReserve: number; usedTokens: number; droppedBlocks: number }
}

export interface AgentExecutionTrace {
  taskId: string
  timeline: AgentTaskEvent[]
  dagNodes: AgentTaskNode[]
  modelCalls: unknown[]
  toolCalls: unknown[]
  ragRetrievals: unknown[]
  memoryAdvisorInjections: unknown[]
  retries: unknown[]
  errors: unknown[]
}

export interface SpringAiToolDescriptor {
  name: string
  description: string
  inputSchema: string
  permissionLevel: string
  readOnly: boolean
  requiresConfirmation: boolean
  timeoutSeconds: number
  maxRetries: number
  idempotent: boolean
  springAiCompatible: boolean
  governance: JsonMap
}
