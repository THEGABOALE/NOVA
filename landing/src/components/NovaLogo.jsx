import novaLogo from "../assets/nova-logo.webp";

function NovaLogo({ className = "" }) {
  return (
    <img
      src={novaLogo}
      alt="NOVA Aplicación Educativa"
      width="552"
      height="144"
      className={`nova-logo ${className}`}
    />
  );
}

export default NovaLogo;
