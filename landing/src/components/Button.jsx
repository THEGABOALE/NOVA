// Enlace con forma de píldora. `primary` es el principal (morado que pasa a
// azul al pasar el mouse) y `secondary` el de borde. `sm` es el tamaño del
// navbar y del pie. El pequeño salto al pasar el mouse no se hace si el
// sistema pide menos movimiento.
const STYLES = {
  "primary-md":
    "rounded-full bg-nova-purple px-8 py-4 font-bold text-white transition hover:bg-nova-blue motion-safe:hover:-translate-y-1",
  "primary-sm":
    "rounded-full bg-nova-purple px-5 py-3 text-sm font-bold text-white transition hover:bg-nova-blue motion-safe:hover:-translate-y-0.5",
  "secondary-md":
    "rounded-full border-2 border-nova-purple px-8 py-4 font-bold text-nova-purple transition hover:bg-nova-purple hover:text-white",
};

function Button({ href, variant = "primary", size = "md", className = "", children, ...props }) {
  const style = STYLES[`${variant}-${size}`];

  return (
    <a href={href} className={className ? `${className} ${style}` : style} {...props}>
      {children}
    </a>
  );
}

export default Button;
