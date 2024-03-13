import * as core from '@actions/core'
import { Nexus2Client } from './nexus2-client'
import {
  ActionOptions,
  Activity,
  PromoteStartRequest,
  Repository
} from './interfaces'
import { findFiles, delayPromise } from './utils'
import { logDebug, logInfo } from './logging'
import axios from 'axios'

/**
 * Create a staging repo and return its id for furher operations.
 */
export async function createStagingRepo(
  nexusClient: Nexus2Client,
  actionOptions: ActionOptions
): Promise<string> {
  logDebug('Resolving staging profile id')
  const stagingProfileId = await nexusClient.getStagingProfileId(
    actionOptions.stagingProfileName
  )
  const data: PromoteStartRequest = {
    data: {
      description: 'Create repo'
    }
  }
  logDebug('Creating staging repo')
  const startResponse = await nexusClient.createStagingRepo(
    stagingProfileId,
    data
  )
  core.setOutput('staged-repository-id', startResponse.data.stagedRepositoryId)
  return startResponse.data.stagedRepositoryId
}

/**
 * Upload all files from a give directory into a staging repository.
 */
export async function uploadFiles(
  nexusClient: Nexus2Client,
  dir: string,
  stagedRepositoryId: string,
  uploadParallel: number
): Promise<void> {
  logDebug(`Uploading files to staging repo, uploadParallel=${uploadParallel}`)
  const files = await findFiles(dir)
  const len = files.length
  // do uploads in defined chunks
  for (let i = 0; i < len; i += uploadParallel) {
    const promises = files
      .slice(i, i + uploadParallel)
      .map(file => nexusClient.deployByRepository(file, stagedRepositoryId))
    await Promise.all(promises)
  }
}

/**
 * Close staging repository.
 */
export async function closeStagingRepo(
  nexusClient: Nexus2Client,
  actionOptions: ActionOptions,
  stagedRepositoryId: string
): Promise<void> {
  logDebug('Closing staging repo')
  const stagingProfileId = await nexusClient.getStagingProfileId(
    actionOptions.stagingProfileName
  )
  await nexusClient.closeStagingRepo(stagingProfileId, {
    data: {
      description: 'Close repo',
      stagedRepositoryId
    }
  })
}

/**
 * Release staging repository.
 */
export async function releaseStagingRepo(
  nexusClient: Nexus2Client,
  actionOptions: ActionOptions,
  stagedRepositoryId: string
): Promise<void> {
  logDebug('Releasing staging repo')
  await nexusClient.bulkPromoteStagingRepos({
    data: {
      description: 'Release repo',
      stagedRepositoryIds: [stagedRepositoryId],
      autoDropAfterRelease: actionOptions.releaseAutoDrop
    }
  })
}

/**
 * Drop staging repository.
 */
export async function dropStagingRepo(
  nexusClient: Nexus2Client,
  stagedRepositoryId: string
): Promise<void> {
  logDebug('Dropping staging repo')
  await nexusClient.dropStagingRepo(stagedRepositoryId, {
    data: {
      description: 'Release repo',
      stagedRepositoryId
    }
  })
}

/**
 * Get repo activity.
 */
export async function stagingRepositoryActivity(
  nexusClient: Nexus2Client,
  stagedRepositoryId: string
): Promise<Activity[]> {
  logDebug('Getting staging repo activity')
  return await nexusClient.stagingRepositoryActivity(stagedRepositoryId)
}

/**
 * Wait with a given timeout for a repo state.
 */
export async function waitRepoState(
  nexusClient: Nexus2Client,
  repositoryIdKey: string,
  state: string,
  timeout: number,
  sleep: number = 10000
): Promise<void> {
  return new Promise(async (resolve, reject) => {
    let now = Date.now()
    const until = now + timeout
    logDebug(`Waiting until ${new Date(until)} from now ${new Date(now)}`)
    let seenRepo = false
    while (until > now) {
      // when repo goes away, i.e. with auto-drop, underlying rest call
      // will return 404, so don't throw if we've seen repo being there
      // first time and then assume it's state is ok.
      let repository: Repository | undefined
      try {
        repository = await nexusClient.stagingRepository(repositoryIdKey)
        seenRepo = true
      } catch (error) {
        if (axios.isAxiosError(error)) {
          if (error.response?.status === 404 && seenRepo) {
            resolve()
            return
          }
        }
      }
      if (repository) {
        logInfo(`Repo state ${repository.type}`)
        if (repository && repository.type === state) {
          resolve()
          return
        }
        if (repository.notifications > 0) {
          reject(
            new Error(
              `Last operation failed with ${repository.notifications} notifications`
            )
          )
          return
        }
        await delayPromise(sleep)
        now = Date.now()
      }
    }
    reject(new Error('Timeout waiting state'))
  })
}
