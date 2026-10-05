import novaIcon from "../assets/nova-icon.webp";

// Las imágenes ya vienen con fondo transparente y al tamaño en que se usan.
// Los originales en alta resolución están en landing/design/.
function NovaMark({ className = "" }) {
  return (
    <img
      src={novaIcon}
      alt=""
      width="256"
      height="256"
      loading="lazy"
      decoding="async"
      className={`nova-mark ${className}`}
    />
  );
}

export default NovaMark;
