import nock from 'nock'
import * as core from '@actions/core'
import * as github from '@actions/github'
import * as nexusSync from '../src/nexus-sync'
import * as nexusUtils from '../src/nexus-utils'
import * as utils from '../src/utils'
import {
  Activity,
  DEFAULT_GENERATE_CHECKSUM_CONFIG,
  GenerateChecksum
} from '../src/interfaces'

const originalGitHubWorkspace = process.env['GITHUB_WORKSPACE']
const originalGitHubRepository = process.env['GITHUB_REPOSITORY']
let originalContext = { ...github.context }
let inputs = {} as any

describe('nexus-sync tests', () => {
  let createStagingRepoSpy: jest.SpyInstance<Promise<string>>
  let uploadFilesSpy: jest.SpyInstance<Promise<void>>
  let closeStagingRepoSpy: jest.SpyInstance<Promise<void>>
  let releaseStagingRepoSpy: jest.SpyInstance<Promise<void>>
  let dropStagingRepoSpy: jest.SpyInstance<Promise<void>>
  let stagingRepositoryActivitySpy: jest.SpyInstance<Promise<Activity[]>>
  let waitRepoStateSpy: jest.SpyInstance<Promise<void>>
  // let generatePgpFilesSpy: jest.SpyInstance<Promise<void>>;
  let generateChecksumFilesSpy: jest.SpyInstance<Promise<void>>

  beforeAll(async () => {
    jest.spyOn(core, 'getInput').mockImplementation((name: string) => {
      return inputs[name]
    })
  }, 300000)

  beforeEach(() => {
    inputs = {}
    nock.cleanAll()
    createStagingRepoSpy = jest
      .spyOn(nexusUtils, 'createStagingRepo')
      .mockImplementation(() => {
        return Promise.resolve('test-123')
      })
    uploadFilesSpy = jest
      .spyOn(nexusUtils, 'uploadFiles')
      .mockImplementation(() => {
        return Promise.resolve()
      })
    closeStagingRepoSpy = jest
      .spyOn(nexusUtils, 'closeStagingRepo')
      .mockImplementation(() => {
        return Promise.resolve()
      })
    releaseStagingRepoSpy = jest
      .spyOn(nexusUtils, 'releaseStagingRepo')
      .mockImplementation(() => {
        return Promise.resolve()
      })
    dropStagingRepoSpy = jest
      .spyOn(nexusUtils, 'dropStagingRepo')
      .mockImplementation(() => {
        return Promise.resolve()
      })
    stagingRepositoryActivitySpy = jest
      .spyOn(nexusUtils, 'stagingRepositoryActivity')
      .mockImplementation(() => {
        return Promise.resolve([])
      })
    waitRepoStateSpy = jest
      .spyOn(nexusUtils, 'waitRepoState')
      .mockImplementation(() => {
        return Promise.resolve()
      })
    // generatePgpFilesSpy = jest.spyOn(utils, 'generatePgpFiles').mockImplementation(() => {
    //   return Promise.resolve();
    // });
    generateChecksumFilesSpy = jest
      .spyOn(utils, 'generateChecksumFiles')
      .mockImplementation(() => {
        return Promise.resolve()
      })
  })

  afterAll(async () => {
    delete process.env['GITHUB_WORKSPACE']
    if (originalGitHubWorkspace) {
      process.env['GITHUB_WORKSPACE'] = originalGitHubWorkspace
    }
    delete process.env['GITHUB_REPOSITORY']
    if (originalGitHubRepository) {
      process.env['GITHUB_REPOSITORY'] = originalGitHubRepository
    }

    github.context.ref = originalContext.ref
    github.context.sha = originalContext.sha

    jest.restoreAllMocks()
  }, 100000)

  it('Missing checksum config should use default', async () => {
    process.env['GITHUB_REPOSITORY'] = 'owner/repo'
    inputs['generate-checksums'] = 'true'
    await nexusSync.run()
    expect(createStagingRepoSpy).not.toHaveBeenCalled()
    expect(uploadFilesSpy).not.toHaveBeenCalled()
    expect(closeStagingRepoSpy).not.toHaveBeenCalled()
    expect(releaseStagingRepoSpy).not.toHaveBeenCalled()
    expect(dropStagingRepoSpy).not.toHaveBeenCalled()
    expect(stagingRepositoryActivitySpy).not.toHaveBeenCalled()
    expect(waitRepoStateSpy).not.toHaveBeenCalled()
    // expect(generatePgpFilesSpy).not.toHaveBeenCalled();
    expect(generateChecksumFilesSpy).toHaveBeenCalledWith(
      'nexus',
      DEFAULT_GENERATE_CHECKSUM_CONFIG
    )
  }, 100000)

  it('Defined checksum config should override default', async () => {
    const checksumConfig: GenerateChecksum[] = [
      { type: 'foo', extension: 'foo' }
    ]
    const checksumConfigInput = JSON.stringify(checksumConfig)
    process.env['GITHUB_REPOSITORY'] = 'owner/repo'
    inputs['generate-checksums'] = 'true'
    inputs['generate-checksums-config'] = checksumConfigInput
    await nexusSync.run()
    expect(createStagingRepoSpy).not.toHaveBeenCalled()
    expect(uploadFilesSpy).not.toHaveBeenCalled()
    expect(closeStagingRepoSpy).not.toHaveBeenCalled()
    expect(releaseStagingRepoSpy).not.toHaveBeenCalled()
    expect(dropStagingRepoSpy).not.toHaveBeenCalled()
    expect(stagingRepositoryActivitySpy).not.toHaveBeenCalled()
    expect(waitRepoStateSpy).not.toHaveBeenCalled()
    // expect(generatePgpFilesSpy).not.toHaveBeenCalled();
    expect(generateChecksumFilesSpy).toHaveBeenCalledWith(
      'nexus',
      checksumConfig
    )
  }, 100000)
})
