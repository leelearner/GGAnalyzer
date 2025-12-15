import { useState, useEffect } from 'react'
import { useQuery } from '@tanstack/react-query'
import axios from 'axios'
import { Calendar } from 'lucide-react'

export default function Standings() {
    const [selectedStage, setSelectedStage] = useState(null);

    // Fetch stages
    const { data: stagesData } = useQuery({
        queryKey: ['stages'],
        queryFn: async () => {
            const response = await axios.get('http://localhost:8080/api/stages')
            return response.data
        }
    })

    const stages = stagesData ? stagesData.map(s => s.name).sort() : [];

    // Set default stage if not set
    useEffect(() => {
        if (stages.length > 0 && !selectedStage) {
            setSelectedStage(stages[0]);
        }
    }, [stages, selectedStage]);

    const { data: standings, isLoading } = useQuery({
        queryKey: ['standings', selectedStage],
        queryFn: async () => {
            if (!selectedStage) return [];
            const res = await axios.get(`http://localhost:8080/api/standings?stage=${selectedStage}`)
            return res.data
        },
        enabled: !!selectedStage
    })

    if (isLoading) return <div>Loading...</div>

    return (
        <div className="flex h-[calc(100vh-100px)]">
            {/* Left Sidebar for Stages */}
            <div className="w-64 pr-6 border-r border-gray-700 mr-6 shrink-0 overflow-y-auto">
                <h3 className="text-lg font-bold mb-4 text-gray-300 px-2 flex items-center">
                    <Calendar className="mr-2 w-5 h-5" /> Stages
                </h3>
                <div className="space-y-1">
                    {stages.map(s => (
                        <button
                            key={s}
                            onClick={() => setSelectedStage(s)}
                            className={`block w-full text-left px-4 py-2 rounded transition-colors ${selectedStage === s ? 'bg-gg-blue text-white' : 'text-gray-400 hover:bg-gray-800 hover:text-white'}`}
                        >
                            {s.replace(/_/g, ' ')
                                .replace(/^(\w+)/, c => c.toUpperCase())
                                .replace(/\b(\w)/g, c => c.toUpperCase())
                                .replace(/(\d)\s+(\d)/, '$1-$2')}
                        </button>
                    ))}
                    {stages.length === 0 && <p className="text-gray-500 px-4">No stages found.</p>}
                </div>
            </div>

            {/* Main Content */}
            <div className="flex-1 min-w-0 flex flex-col">
                <h2 className="text-2xl font-bold mb-6">Standings</h2>

                {!standings || standings.length === 0 ? (
                    <div className="bg-gg-card rounded-lg p-8 text-center text-gray-400">
                        <p>No standings data available for this stage.</p>
                    </div>
                ) : (
                    <div className="bg-gg-card rounded-lg overflow-hidden">
                        <table className="w-full text-left">
                            <thead className="bg-gray-800 text-gray-400 text-sm uppercase">
                                <tr>
                                    <th className="p-4">Rank</th>
                                    <th className="p-4">Team</th>
                                    <th className="p-4">W-L</th>
                                    <th className="p-4">Point Diff</th>
                                    <th className="p-4">Win Rate</th>
                                    <th className="p-4">Streak</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-gray-700">
                                {standings.map((team) => (
                                    <tr key={team.teamAcronym} className="hover:bg-gray-700 transition">
                                        <td className="p-4 font-bold text-lg">{team.rank}</td>
                                        <td className="p-4 flex items-center space-x-3">
                                            {team.team.logoUrl ? (
                                                <img src={team.team.logoUrl} alt={team.teamAcronym} className="w-8 h-8 rounded-full" />
                                            ) : (
                                                <div className="w-8 h-8 bg-gray-600 rounded-full"></div>
                                            )}
                                            <span className="font-bold">{team.teamName}</span>
                                        </td>
                                        <td className="p-4">{team.wins} - {team.losses}</td>
                                        <td className="p-4 text-gg-blue">{team.pointDiff > 0 ? '+' : ''}{team.pointDiff}</td>
                                        <td className="p-4">{team.winRate}</td>
                                        <td className="p-4">{team.streak}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    )
}
