import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Layout from './components/Layout'
import Schedule from './pages/Schedule'
import Standings from './pages/Standings'
import Stats from './pages/Stats'

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<Layout />}>
                    <Route index element={<Schedule />} />
                    <Route path="standings" element={<Standings />} />
                    <Route path="stats" element={<Stats />} />
                </Route>
            </Routes>
        </BrowserRouter>
    )
}

export default App