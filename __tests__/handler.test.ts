import nock from 'nock'
import * as handler from '../src/handler'
import * as nexusUtils from '../src/nexus-utils'
import * as utils from '../src/utils'
import {
  Activity,
  ActionOptions,
  BulkPromoteResponse,
  DEFAULT_GENERATE_CHECKSUM_CONFIG,
  Repository
} from '../src/interfaces'

describe('handler tests', () => {
  beforeEach(() => {
    nock.cleanAll()
  })

  afterAll(async () => {
    jest.restoreAllMocks()
  }, 100000)

  // There seems to be a random time nexus is throwing out
  // 500 for a release request when it reports repository
  // has been closed successfully.
  // We've seen up to 60 seconds when nexus having a bad day.
  it('Nexus throws errors after close when requesting release', async () => {
    const actionOptions: ActionOptions = {
      create: false,
      stagingProfileName: 'test',
      stagingRepoId: 'fake',
      upload: false,
      uploadParallel: 1,
      close: false,
      closeTimeout: 1000,
      dropIfFailure: false,
      release: true,
      releaseAutoDrop: false,
      releaseTimeout: 25000,
      dir: 'nexus',
      nexusServer: {
        url: 'http://example.com',
        username: 'admin',
        password: 'admin123',
        timeout: 60000
      },
      generateChecksums: false,
      generateChecksumsConfig: DEFAULT_GENERATE_CHECKSUM_CONFIG
    }

    let releaseStagingRepoSpy: jest.SpyInstance<Promise<void>>
    let waitRepoStateSpy: jest.SpyInstance<Promise<void>>

    releaseStagingRepoSpy = jest.spyOn(nexusUtils, 'releaseStagingRepo')

    waitRepoStateSpy = jest.spyOn(nexusUtils, 'waitRepoState')

    const RES1: BulkPromoteResponse = {}

    const RES1_ERROR: BulkPromoteResponse = {}

    nock('http://example.com')
      .post('/service/local/staging/bulk/promote')
      .times(2)
      .reply(500)
    nock('http://example.com')
      .post('/service/local/staging/bulk/promote')
      .reply(201, RES1)

    const RES2_CLOSED: Repository = {
      notifications: 0,
      type: 'closed'
    }

    const RES2_RELEASED: Repository = {
      notifications: 0,
      type: 'released'
    }

    nock('http://example.com')
      .get('/service/local/staging/repository/fake')
      .times(1)
      .reply(201, RES2_CLOSED)
    nock('http://example.com')
      .get('/service/local/staging/repository/fake')
      .times(1)
      .reply(201, RES2_RELEASED)

    await handler.handle(actionOptions)

    expect(releaseStagingRepoSpy).toHaveBeenCalled()
    expect(waitRepoStateSpy).toHaveBeenCalled()
  }, 32000)

  it('Release have closed in first state request', async () => {
    const actionOptions: ActionOptions = {
      create: false,
      stagingProfileName: 'test',
      stagingRepoId: 'fake',
      upload: false,
      uploadParallel: 1,
      close: false,
      closeTimeout: 1000,
      dropIfFailure: false,
      release: true,
      releaseAutoDrop: false,
      releaseTimeout: 25000,
      dir: 'nexus',
      nexusServer: {
        url: 'http://example.com',
        username: 'admin',
        password: 'admin123',
        timeout: 1000
      },
      generateChecksums: false,
      generateChecksumsConfig: DEFAULT_GENERATE_CHECKSUM_CONFIG
    }

    let releaseStagingRepoSpy: jest.SpyInstance<Promise<void>>
    let waitRepoStateSpy: jest.SpyInstance<Promise<void>>

    releaseStagingRepoSpy = jest.spyOn(nexusUtils, 'releaseStagingRepo')

    waitRepoStateSpy = jest.spyOn(nexusUtils, 'waitRepoState')

    const RES1: BulkPromoteResponse = {}

    nock('http://example.com')
      .post('/service/local/staging/bulk/promote')
      .reply(201, RES1)

    const RES2_CLOSED: Repository = {
      notifications: 0,
      type: 'closed'
    }

    const RES2_RELEASED: Repository = {
      notifications: 0,
      type: 'released'
    }

    nock('http://example.com')
      .get('/service/local/staging/repository/fake')
      .times(1)
      .reply(201, RES2_CLOSED)
    nock('http://example.com')
      .get('/service/local/staging/repository/fake')
      .times(1)
      .reply(201, RES2_RELEASED)

    await handler.handle(actionOptions)

    expect(releaseStagingRepoSpy).toHaveBeenCalled()
    expect(waitRepoStateSpy).toHaveBeenCalled()
  }, 32000)

  it('When all disabled should not call methods', async () => {
    let createStagingRepoSpy: jest.SpyInstance<Promise<string>>
    let uploadFilesSpy: jest.SpyInstance<Promise<void>>
    let closeStagingRepoSpy: jest.SpyInstance<Promise<void>>
    let releaseStagingRepoSpy: jest.SpyInstance<Promise<void>>
    let dropStagingRepoSpy: jest.SpyInstance<Promise<void>>
    let stagingRepositoryActivitySpy: jest.SpyInstance<Promise<Activity[]>>
    let waitRepoStateSpy: jest.SpyInstance<Promise<void>>
    let generateChecksumFilesSpy: jest.SpyInstance<Promise<void>>

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
    generateChecksumFilesSpy = jest
      .spyOn(utils, 'generateChecksumFiles')
      .mockImplementation(() => {
        return Promise.resolve()
      })

    const actionOptions: ActionOptions = {
      create: false,
      stagingProfileName: 'test',
      stagingRepoId: undefined,
      upload: false,
      uploadParallel: 1,
      close: false,
      closeTimeout: 1000,
      dropIfFailure: false,
      release: false,
      releaseAutoDrop: false,
      releaseTimeout: 1000,
      dir: 'nexus',
      nexusServer: {
        url: 'http://example.com',
        username: 'admin',
        password: 'admin123',
        timeout: 1000
      },
      generateChecksums: false,
      generateChecksumsConfig: DEFAULT_GENERATE_CHECKSUM_CONFIG
    }
    await handler.handle(actionOptions)
    expect(createStagingRepoSpy).not.toHaveBeenCalled()
    expect(uploadFilesSpy).not.toHaveBeenCalled()
    expect(closeStagingRepoSpy).not.toHaveBeenCalled()
    expect(releaseStagingRepoSpy).not.toHaveBeenCalled()
    expect(dropStagingRepoSpy).not.toHaveBeenCalled()
    expect(stagingRepositoryActivitySpy).not.toHaveBeenCalled()
    expect(waitRepoStateSpy).not.toHaveBeenCalled()
    expect(generateChecksumFilesSpy).not.toHaveBeenCalledWith(
      'nexus',
      DEFAULT_GENERATE_CHECKSUM_CONFIG
    )
  }, 100000)
})
