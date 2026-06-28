import type { Config } from "tailwindcss";
const config: Config = {
  content: ["./src/**/*.{js,ts,jsx,tsx,mdx}"],
  theme: {
    extend: {
      colors: {
        primary: { DEFAULT: "#1B4F8A", light: "#2D6CC0", dark: "#0F3260" },
        secondary: { DEFAULT: "#00897B" },
        accent: { DEFAULT: "#E3F2FD" },
      },
      fontFamily: {
        sans: ["Plus Jakarta Sans", "Inter", "system-ui", "sans-serif"],
        mono: ["IBM Plex Mono", "monospace"],
      },
    },
  },
  plugins: [],
};
export default config;
