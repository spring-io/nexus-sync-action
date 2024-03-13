import { inspect } from 'util'
import { ActionOptions } from './interfaces'
import { endGroup, logDebug, logInfo, startGroup } from './logging'
import {
  closeStagingRepo,
  createStagingRepo,
  dropStagingRepo,
  releaseStagingRepo,
  uploadFiles,
  waitRepoState,
  stagingRepositoryActivity
} from './nexus-utils'
import { Nexus2Client } from './nexus2-client'
import { generateChecksumFiles, delayPromise } from './utils'

export async function handle(actionOptions: ActionOptions): Promise<void> {
  const nexusClient = new Nexus2Client(actionOptions.nexusServer)
  logDebug(
    `Using nexus server ${actionOptions.nexusServer.url} with timeout ${actionOptions.nexusServer.timeout}`
  )

  // initial state calculated from a given options
  const handlerState: HandlerState = {
    needChecksum: actionOptions.generateChecksums,
    needCreate: actionOptions.create,
    stagingRepoId: actionOptions.stagingRepoId,
    needUpload: actionOptions.upload,
    needClose: actionOptions.close,
    needRelease: actionOptions.release,
    needDrop: actionOptions.dropIfFailure
  }

  // need to checksum
  if (handlerState.needChecksum) {
    startGroup('Checksums')
    logInfo(
      `Generation with config ${inspect(actionOptions.generateChecksumsConfig)}`
    )
    await generateChecksumFiles(
      actionOptions.dir,
      actionOptions.generateChecksumsConfig
    )
    endGroup()
  }

  // if there's a need to create a repo
  if (handlerState.needCreate) {
    startGroup('Staging Repo Create')
    const stagedRepositoryId = await createStagingRepo(
      nexusClient,
      actionOptions
    )
    handlerState.stagingRepoId = stagedRepositoryId
    logInfo(`Created repo ${stagedRepositoryId}`)
    endGroup()
  }

  // need to upload files
  if (handlerState.needUpload && handlerState.stagingRepoId) {
    startGroup('File Upload')
    await uploadFiles(
      nexusClient,
      actionOptions.dir,
      handlerState.stagingRepoId,
      actionOptions.uploadParallel
    )
    endGroup()
  }

  // need to close
  if (handlerState.needClose && handlerState.stagingRepoId) {
    startGroup('Staging Repo Close')
    await closeStagingRepo(
      nexusClient,
      actionOptions,
      handlerState.stagingRepoId
    )
    logInfo(`Closed repo ${handlerState.stagingRepoId}`)
    logInfo(`Waiting repo ${handlerState.stagingRepoId} state closed`)
    try {
      await waitRepoState(
        nexusClient,
        handlerState.stagingRepoId,
        'closed',
        actionOptions.closeTimeout
      )
    } catch (error) {
      await logActivity(nexusClient, handlerState.stagingRepoId)
      if (handlerState.needDrop) {
        await dropRepo(nexusClient, handlerState.stagingRepoId)
      }
      throw error
    }
    endGroup()
  }

  // need to release
  if (handlerState.needRelease && handlerState.stagingRepoId) {
    startGroup('Staging Repo Release')
    // looks like we get 500 if release is done too quickly
    // after close so instead of implementing full retry, just sleep
    // a bit before going into release request
    if (handlerState.needClose) {
      await delayPromise(10000)
    }
    try {
      await releaseStagingRepo(
        nexusClient,
        actionOptions,
        handlerState.stagingRepoId
      )
    } catch (error) {
      await logActivity(nexusClient, handlerState.stagingRepoId)
      throw error
    }
    logInfo(`Released repo ${handlerState.stagingRepoId}`)
    logInfo(`Waiting repo ${handlerState.stagingRepoId} state released`)
    try {
      await waitRepoState(
        nexusClient,
        handlerState.stagingRepoId,
        'released',
        actionOptions.releaseTimeout
      )
    } catch (error) {
      await logActivity(nexusClient, handlerState.stagingRepoId)
      throw error
    }
    endGroup()
  }
}

async function logActivity(
  nexusClient: Nexus2Client,
  stagingRepoId: string
): Promise<void> {
  try {
    const activities = await stagingRepositoryActivity(
      nexusClient,
      stagingRepoId
    )
    logInfo(`Repo activities ${inspect(activities, false, 10)}`)
  } catch (error) {}
}

async function dropRepo(
  nexusClient: Nexus2Client,
  stagingRepoId: string
): Promise<void> {
  try {
    logInfo(`Dropping repo ${stagingRepoId}`)
    await dropStagingRepo(nexusClient, stagingRepoId)
  } catch (error) {}
}

interface HandlerState {
  needChecksum: boolean
  needCreate: boolean
  stagingRepoId: string | undefined
  needUpload: boolean
  needClose: boolean
  needRelease: boolean
  needDrop: boolean
}
