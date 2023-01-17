const colors = require("tailwindcss/colors")

module.exports = {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
    "./node_modules/vue-tailwind-datepicker/**/*.js"
  ],
  theme: {
    extend: {
      colors: {
        "vtd-primary": colors.sky,
        "vtd-secondary": colors.white,
      },
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
  ]
}