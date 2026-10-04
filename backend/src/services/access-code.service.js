// Por qué no sirve un código de sala. El estudiante necesita saber si lo
// escribió mal, si venció o si se acabaron los usos: cada caso se arregla
// distinto (revisar lo escrito o pedir uno nuevo).

const REASONS = {
  CODE_NOT_FOUND: "No encontramos una sala con ese código. Revisa que esté bien escrito.",
  CODE_INACTIVE: "Este código ya no está activo. Pídele uno nuevo a tu docente.",
  CODE_EXPIRED: "Este código ya venció. Pídele uno nuevo a tu docente.",
  CODE_EXHAUSTED: "Este código ya se usó todas las veces permitidas. Pídele uno nuevo a tu docente."
};

const reason = (code) => ({ code, message: REASONS[code] });

/**
 * null si el código sirve; si no, { code, message } con el motivo. Si venció
 * lo calcula la consulta (is_expired) con el reloj de la base, porque
 * expires_at se guarda sin zona horaria.
 */
const classifyAccessCode = (row) => {
  if (!row || !row.group_is_active) return reason("CODE_NOT_FOUND");
  if (!row.code_is_active) return reason("CODE_INACTIVE");
  if (row.is_expired) return reason("CODE_EXPIRED");
  if (row.max_uses !== null && row.max_uses !== undefined && row.current_uses >= row.max_uses) {
    return reason("CODE_EXHAUSTED");
  }
  return null;
};

module.exports = { classifyAccessCode };
