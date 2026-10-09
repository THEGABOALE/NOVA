// Enlace con forma de píldora. `primary` es el principal (morado que pasa a
// azul al pasar el mouse) y `secondary` el de borde. `sm` es el tamaño del
// navbar y del pie.
const STYLES = {
  "primary-md":
    "rounded-full bg-nova-purple px-8 py-4 font-bold text-white transition hover:-translate-y-1 hover:bg-nova-blue",
  "primary-sm":
    "rounded-full bg-nova-purple px-5 py-3 text-sm font-bold text-white transition hover:-translate-y-0.5 hover:bg-nova-blue",
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
