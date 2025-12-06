import { useQuery } from '@tanstack/react-query'
import axios from 'axios'
import { Calendar } from 'lucide-react'

export default function Schedule() {
    const { data: matches, isLoading, error } = useQuery({
        queryKey: ['matches'],
        queryFn: async () => {
            const response = await axios.get('http://localhost:8080/api/matches')
            return response.data
        }
    })

    return (
        <div>
            <div className="flex justify-between items-center mb-6">
                <h2 className="text-xl font-semibold flex items-center">
                    <Calendar className="mr-2 w-5 h-5" /> Match Schedule
                </h2>
            </div>

            {isLoading && <p>Loading matches...</p>}
            {error && <p className="text-red-500">Error loading matches. Is the backend running?</p>}

            <div className="grid gap-4">
                {matches && matches.length === 0 && (
                    <div className="bg-gg-card rounded-lg p-8 text-center text-gray-400">
                        <p>No matches scheduled yet.</p>
                    </div>
                )}
                {matches && matches.map(match => (
                    <div key={match.id} className="bg-gg-card p-4 rounded-lg flex justify-between items-center hover:bg-gray-700 transition">
                        <div className="flex items-center space-x-4 w-1/3">
                            <span className="text-gray-400 text-sm">{new Date(match.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                            <span className="font-bold">{match.league?.name}</span>
                        </div>

                        <div className="flex items-center justify-center space-x-8 w-1/3">
                            <div className="flex items-center space-x-2">
                                <span className="font-bold text-lg">{match.teamA?.acronym}</span>
                                <div className="w-8 h-8 bg-gray-600 rounded-full"></div>
                            </div>
                            <div className="text-xl font-bold text-gray-300">
                                {match.teamAScore} - {match.teamBScore}
                            </div>
                            <div className="flex items-center space-x-2">
                                <div className="w-8 h-8 bg-gray-600 rounded-full"></div>
                                <span className="font-bold text-lg">{match.teamB?.acronym}</span>
                            </div>
                        </div>

                        <div className="w-1/3 text-right">
                            <span className="text-xs bg-gray-800 px-2 py-1 rounded text-gray-400">{match.format}</span>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    )
}
