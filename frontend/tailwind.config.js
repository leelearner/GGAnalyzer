/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            colors: {
                'gg-dark': '#1c1c1f',
                'gg-card': '#282830',
                'gg-blue': '#5383e8',
            }
        },
    },
    plugins: [],
}
