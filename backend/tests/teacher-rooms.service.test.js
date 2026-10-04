const { buildTeacherRooms } = require("../src/services/teacher-rooms.service");

test("reparte estudiantes y promedios en su sala", () => {
  const groups = [
    { id: 3, name: "5A", grade: "5", section: "A", school_year: 2026, level_id: 1 },
    { id: 4, name: "6B", grade: "6", section: "B", school_year: 2026, level_id: 1 }
  ];
  const students = [
    { group_id: 3, id: 10, full_name: "Ana", total_points: "120", missions_completed: "2", last_attempt_at: null, last_attempt_seconds: "41.6" },
    { group_id: 4, id: 11, full_name: "Beto", total_points: "0", missions_completed: "0", last_attempt_at: null, last_attempt_seconds: null }
  ];
  const mechanics = [{ group_id: 3, mechanic: "true_false", average_score: "80" }];

  const rooms = buildTeacherRooms(groups, students, mechanics);

  expect(rooms).toHaveLength(2);
  expect(rooms[0]).toMatchObject({ id: 3, name: "5A", schoolYear: 2026, levelId: 1, studentCount: 1 });
  expect(rooms[0].students[0]).toEqual({
    id: 10, fullName: "Ana", totalPoints: 120, missionsCompleted: 2, lastAttemptAt: null, lastAttemptSeconds: 42
  });
  expect(rooms[0].resultsByMechanic).toEqual([{ mechanic: "true_false", averageScore: 80 }]);
  expect(rooms[1].students[0].lastAttemptSeconds).toBeNull();
  expect(rooms[1].resultsByMechanic).toEqual([]);
});

test("una sala sin estudiantes sale vacía", () => {
  const groups = [{ id: 9, name: "Nueva", grade: "3", section: null, school_year: 2026, level_id: 1 }];

  expect(buildTeacherRooms(groups, [], [])[0]).toMatchObject({ studentCount: 0, students: [], resultsByMechanic: [] });
});

test("sin salas no hay nada que armar", () => {
  expect(buildTeacherRooms([], [], [])).toEqual([]);
});
