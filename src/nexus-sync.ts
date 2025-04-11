import * as core from '@actions/core'
import { inspect } from 'util'
import { handle } from './handler'
import {
  ActionOptions,
  DEFAULT_GENERATE_CHECKSUM_CONFIG,
  GenerateChecksum
} from './interfaces'
import { logDebug } from './logging'
import { numberValue } from './utils'

export async function run() {
  try {
    const username = inputNotRequired('username') || undefined
    const password = inputNotRequired('password') || undefined
    const token = inputNotRequired('token') || undefined
    if (!token) {
      if (!username && !password) {
        throw new Error(
          'Either username and password or token needs to be set.'
        )
      }
    }
    const create = inputNotRequired('create') === 'true' ? true : false
    const stagingProfileName = inputRequired('staging-profile-name')
    const stagingRepoId = inputNotRequired('staging-repo-id') || undefined
    const upload = inputNotRequired('upload') === 'true' ? true : false
    const uploadParallel = numberValue(inputNotRequired('upload-parallel'), 1)
    const close = inputNotRequired('close') === 'true' ? true : false
    const dropIfFailure =
      inputNotRequired('drop-if-failure') === 'true' ? true : false
    const closeTimeout = numberValue(inputNotRequired('close-timeout'), 600)
    const release = inputNotRequired('release') === 'true' ? true : false
    const releaseAutoDrop =
      inputNotRequired('release-auto-drop') === 'false' ? false : true
    const releaseTimeout = numberValue(inputNotRequired('release-timeout'), 600)
    const url = inputNotRequired('url') || 'https://s01.oss.sonatype.org'
    const dir = inputNotRequired('dir') || 'nexus'
    const generateChecksums =
      inputNotRequired('generate-checksums') === 'true' ? true : false
    const generateChecksumsConfigData =
      inputNotRequired('generate-checksums-config') || undefined
    const generateChecksumsConfig: GenerateChecksum[] =
      generateChecksumsConfigData
        ? JSON.parse(generateChecksumsConfigData)
        : DEFAULT_GENERATE_CHECKSUM_CONFIG
    const nexusTimeout = numberValue(inputNotRequired('nexus-timeout'), 0)

    if (uploadParallel < 1) {
      throw new Error(
        `'upload-parallel' needs to be higher than 0, was ${uploadParallel}`
      )
    }

    const actionOptions: ActionOptions = {
      create,
      stagingProfileName,
      stagingRepoId,
      upload,
      uploadParallel,
      close,
      closeTimeout: closeTimeout * 1000,
      dropIfFailure,
      release,
      releaseAutoDrop,
      releaseTimeout: releaseTimeout * 1000,
      dir,
      nexusServer: {
        username,
        password,
        token,
        url,
        timeout: nexusTimeout * 1000
      },
      generateChecksums,
      generateChecksumsConfig
    }
    await handle(actionOptions)
  } catch (error) {
    logDebug(inspect(error))
    if (error instanceof Error) {
      core.setFailed(error.message)
    }
  }
}

function inputRequired(id: string): string {
  return core.getInput(id, { required: true })
}

function inputNotRequired(id: string): string {
  return core.getInput(id, { required: false })
}
