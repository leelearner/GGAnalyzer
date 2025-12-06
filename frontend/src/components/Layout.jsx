import { Link, Outlet } from 'react-router-dom'
import { Trophy, Search } from 'lucide-react'

export default function Layout() {
    return (
        <div className="min-h-screen bg-gg-dark text-white">
            <header className="bg-gg-card border-b border-gray-700 p-4 sticky top-0 z-50">
                <div className="container mx-auto flex justify-between items-center">
                    <Link to="/" className="flex items-center space-x-2">
                        <Trophy className="text-gg-blue w-8 h-8" />
                        <h1 className="text-2xl font-bold">GGAnalyzer</h1>
                    </Link>
                    <nav className="flex space-x-6">
                        <Link to="/" className="hover:text-gg-blue font-medium">Schedule</Link>
                        <Link to="/standings" className="hover:text-gg-blue font-medium">Standings</Link>
                        <Link to="/stats" className="hover:text-gg-blue font-medium">Stats</Link>
                    </nav>
                    <div className="relative">
                        <Search className="absolute left-3 top-2.5 text-gray-400 w-4 h-4" />
                        <input
                            type="text"
                            placeholder="Search teams, players..."
                            className="bg-gray-800 rounded-full py-2 pl-10 pr-4 text-sm focus:outline-none focus:ring-2 focus:ring-gg-blue"
                        />
                    </div>
                </div>
            </header>
            <main className="container mx-auto p-6">
                {/* <main className="mx-auto p-6"> */}
                <Outlet />
            </main>
        </div>
    )
}
