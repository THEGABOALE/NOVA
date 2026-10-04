const { classifyAccessCode } = require("../src/services/access-code.service");

const usable = {
  code_is_active: true,
  group_is_active: true,
  is_expired: false,
  max_uses: null,
  current_uses: 0
};

test("un código vigente no tiene problema", () => {
  expect(classifyAccessCode(usable)).toBeNull();
});

test("sin fila o con la sala inactiva: no existe", () => {
  expect(classifyAccessCode(null).code).toBe("CODE_NOT_FOUND");
  expect(classifyAccessCode({ ...usable, group_is_active: false }).code).toBe("CODE_NOT_FOUND");
});

test("código desactivado", () => {
  expect(classifyAccessCode({ ...usable, code_is_active: false }).code).toBe("CODE_INACTIVE");
});

test("código vencido", () => {
  // Si venció lo decide la base, con su propio reloj (expires_at no tiene zona horaria).
  expect(classifyAccessCode({ ...usable, is_expired: true }).code).toBe("CODE_EXPIRED");
});

test("código sin usos", () => {
  expect(classifyAccessCode({ ...usable, max_uses: 30, current_uses: 30 }).code).toBe("CODE_EXHAUSTED");
  expect(classifyAccessCode({ ...usable, max_uses: 30, current_uses: 29 })).toBeNull();
});

test("cada motivo tiene un mensaje en tuteo que dice qué hacer", () => {
  expect(classifyAccessCode(null).message).toBe(
    "No encontramos una sala con ese código. Revisa que esté bien escrito."
  );
  expect(classifyAccessCode({ ...usable, is_expired: true }).message).toBe(
    "Este código ya venció. Pídele uno nuevo a tu docente."
  );
});
