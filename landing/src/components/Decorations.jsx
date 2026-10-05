// Formas decorativas de la marca. Son solo adorno: aria-hidden.
export function NovaSpark({ className = "" }) {
  return (
    <svg
      viewBox="0 0 80 80"
      aria-hidden="true"
      className={className}
    >
      <path
        d="M40 7C43 25 55 37 73 40C55 43 43 55 40 73C37 55 25 43 7 40C25 37 37 25 40 7Z"
        fill="currentColor"
      />
    </svg>
  );
}

export function NovaDoodle() {
  return (
    <svg
      viewBox="0 0 320 150"
      aria-hidden="true"
      className="h-full w-full"
    >
      <path
        d="M18 105C61 35 111 134 156 68C193 15 249 23 300 86"
        fill="none"
        stroke="currentColor"
        strokeWidth="5"
        strokeLinecap="round"
        strokeDasharray="1 14"
      />

      <circle cx="40" cy="42" r="7" fill="currentColor" />
      <circle cx="272" cy="30" r="5" fill="currentColor" />

      <path
        d="M226 106C231 88 247 81 260 91C247 93 238 101 226 106Z"
        fill="currentColor"
      />
    </svg>
  );
}
