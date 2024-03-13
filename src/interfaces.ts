/**
 * Possible error fields if any. Some responses don't usually return data, but may return error fields.
 */
export interface ErrorResponse {
  errors?: [
    {
      id: string
      msg: string
    }
  ]
}

/**
 * When requesting profiles, were interested of id/name pairs.
 */
export interface StagingProfilesResponse extends ErrorResponse {
  data: [
    {
      id: string
      name: string
    }
  ]
}

/**
 * Promote start just needs description.
 */
export interface PromoteStartRequest {
  data: {
    description: string
  }
}

/**
 * Promote start response gives us repo id.
 */
export interface PromoteStartResponse extends ErrorResponse {
  data: {
    description: string
    stagedRepositoryId: string
  }
}

/**
 * Closing aka finishing staging repo needs staging repo id, target repo is and optional description.
 */
export interface PromoteFinishRequest {
  data: {
    stagedRepositoryId: string
    targetRepositoryId?: string
    description: string
  }
}

/**
 * Closing aka finishing a staging repo will not return data unless there were errors.
 */
export interface PromoteFinishResponse extends ErrorResponse {}

/**
 * Bulk promote staging repo needs staging repo id, target repo is and optional description.
 */
export interface BulkPromoteRequest {
  data: {
    stagedRepositoryIds: string[]
    autoDropAfterRelease: boolean
    description: string
  }
}

/**
 * Bulk promote a staging repos will not return data unless there were errors.
 */
export interface BulkPromoteResponse extends ErrorResponse {}

/**
 * Drop staging repo needs staging repo id, target repo is and optional description.
 */
export interface PromoteDropRequest {
  data: {
    stagedRepositoryId: string
    targetRepositoryId?: string
    description: string
  }
}

/**
 * Promote a staging repo will not return data unless there were errors.
 */
export interface PromoteDropResponse extends ErrorResponse {}

/**
 * Keep nexus server setting together.
 */
export interface NexusServer {
  url: string
  username: string
  password: string
  timeout: number
}

export interface GenerateChecksum {
  type: string
  extension: string
}

/**
 * Default checksum config.
 */
export const DEFAULT_GENERATE_CHECKSUM_CONFIG: GenerateChecksum[] = [
  { type: 'md5', extension: 'md5' },
  { type: 'sha1', extension: 'sha1' }
]

/**
 * Options for action to ease passing those around.
 */
export interface ActionOptions {
  create: boolean
  stagingProfileName: string
  stagingRepoId: string | undefined
  upload: boolean
  uploadParallel: number
  close: boolean
  closeTimeout: number
  dropIfFailure: boolean
  release: boolean
  releaseAutoDrop: boolean
  releaseTimeout: number
  dir: string
  nexusServer: NexusServer
  generateChecksums: boolean
  generateChecksumsConfig: GenerateChecksum[]
}

export interface Repository extends ErrorResponse {
  notifications: number
  type: string
}

export interface Activity {
  events: Event[]
  name: string
  started: string
  stopped: string
}

export interface Event {
  name: string
  properties: Map<string, string>
}

/**
 * Represents a file to get uploaded to a staging repo, where
 * path is a real os path, name as a file name and group is a
 * escaped group id accepted by nexus requests.
 */
export interface UploadFile {
  path: string
  name: string
  group: string
}
