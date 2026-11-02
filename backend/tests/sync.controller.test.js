const fakePool = { name: "pool" };
const fakeDb = { name: "transaction" };

jest.mock("../src/database/connection", () => fakePool);

jest.mock("../src/database/transaction", () => ({
  withTransaction: jest.fn(async (callback) => callback(fakeDb))
}));

jest.mock("../src/repositories/mission.repository", () => ({
  findPublishedMission: jest.fn(),
  findPreviousMission: jest.fn()
}));

jest.mock("../src/repositories/attempt.repository", () => ({
  findByClientAttemptId: jest.fn(),
  hasCompletedMission: jest.fn(),
  findStudentTotals: jest.fn(),
  findCompletedMissionIds: jest.fn(),
  insertSettledAttempt: jest.fn()
}));

jest.mock("../src/repositories/group.repository", () => ({
  findActiveGroup: jest.fn()
}));

jest.mock("../src/repositories/user.repository", () => ({
  lockUser: jest.fn()
}));

jest.mock("../src/services/mission-access.service", () => ({
  missionStartBlock: jest.fn()
}));

jest.mock("../src/services/attempt-settlement.service", () => ({
  settleAttempt: jest.fn()
}));

jest.mock("../src/services/attempt-sync.service", () => ({
  parseSyncBatch: jest.fn(),
  checkAttemptTimes: jest.fn()
}));

jest.mock("../src/services/mission-grading.service", () => ({
  playedSeconds: jest.fn()
}));

jest.mock("../src/services/streak-query.service", () => ({
  loadStreak: jest.fn()
}));

jest.mock("../src/services/teacher-cache.service", () => ({
  invalidateTeacherStudentsForStudent: jest.fn()
}));

jest.mock("../src/utils/server-error", () => ({
  respondServerError: jest.fn()
}));

const missionRepository = require("../src/repositories/mission.repository");
const attemptRepository = require("../src/repositories/attempt.repository");
const groupRepository = require("../src/repositories/group.repository");

const {
  missionStartBlock
} = require("../src/services/mission-access.service");

const {
  settleAttempt
} = require("../src/services/attempt-settlement.service");

const {
  parseSyncBatch,
  checkAttemptTimes
} = require("../src/services/attempt-sync.service");

const {
  playedSeconds
} = require("../src/services/mission-grading.service");

const {
  loadStreak
} = require("../src/services/streak-query.service");

const {
  invalidateTeacherStudentsForStudent
} = require("../src/services/teacher-cache.service");

const {
  syncAttempts
} = require("../src/controllers/sync.controller");

describe("sync.controller", () => {
  let req;
  let res;

  beforeEach(() => {
    jest.clearAllMocks();

    req = {
      user: {
        id: 2
      },
      body: {}
    };

    res = {
      status: jest.fn().mockReturnThis(),
      json: jest.fn()
    };

    parseSyncBatch.mockReturnValue({
      tzOffsetMinutes: -360,
      attempts: [
        {
          clientAttemptId: "11111111-1111-4111-8111-111111111111",
          missionId: 1,
          startedAt: "2026-11-02T15:00:00.000Z",
          finishedAt: "2026-11-02T15:02:00.000Z",
          answers: [],
          timedOut: false,
          pausedSeconds: 0,
          usedExtraTime: false
        },
        {
          clientAttemptId: "22222222-2222-4222-8222-222222222222",
          missionId: 1,
          startedAt: "2026-11-02T15:03:00.000Z",
          finishedAt: "2026-11-02T15:05:00.000Z",
          answers: [],
          timedOut: false,
          pausedSeconds: 0,
          usedExtraTime: false
        }
      ]
    });

    checkAttemptTimes.mockReturnValue(null);

    attemptRepository.findByClientAttemptId.mockResolvedValue(null);

    missionRepository.findPublishedMission.mockResolvedValue({
      id: 1,
      level_id: 1,
      order_index: 1,
      points_reward: 20,
      max_plumas: 3,
      time_limit_seconds: 300
    });

    groupRepository.findActiveGroup.mockResolvedValue({
      id: 1
    });

    missionRepository.findPreviousMission.mockResolvedValue(null);

    missionStartBlock.mockReturnValue(null);

    playedSeconds.mockReturnValue(120);

    settleAttempt.mockResolvedValue({
      missionId: 1,
      score: 100,
      correctAnswers: 2,
      wrongAnswers: 0,
      plumasLeft: 3,
      pointsEarned: 20,
      isReview: false,
      status: "completed",
      seedsSpent: 0
    });

    loadStreak
      .mockResolvedValueOnce({
        streak: {
          days: 0,
          isActive: false,
          lastActivityDate: null
        },
        attemptsToday: 0
      })
      .mockResolvedValueOnce({
        streak: {
          days: 1,
          isActive: true,
          lastActivityDate: "2026-11-02"
        },
        attemptsToday: 2
      });

    attemptRepository.findStudentTotals.mockResolvedValue({
      totalPoints: 40,
      missionsCompleted: 1
    });

    attemptRepository.findCompletedMissionIds.mockResolvedValue([1]);

    invalidateTeacherStudentsForStudent.mockResolvedValue(true);
  });

  test("invalida una sola vez el cache docente después de sincronizar el lote", async () => {
    await syncAttempts(req, res);

    expect(invalidateTeacherStudentsForStudent).toHaveBeenCalledTimes(1);

    expect(
      invalidateTeacherStudentsForStudent
    ).toHaveBeenCalledWith(fakePool, 2);

    expect(res.status).toHaveBeenCalledWith(200);
  });
});