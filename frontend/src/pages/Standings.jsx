import { useQuery } from '@tanstack/react-query'
import axios from 'axios'

export default function Standings() {
    const { data: standings, isLoading } = useQuery({
        queryKey: ['standings'],
        queryFn: async () => {
            const res = await axios.get('http://localhost:8080/api/standings')
            return res.data
        }
    })

    if (isLoading) return <div>Loading...</div>

    if (!standings || standings.length === 0) {
        return (
            <div>
                <h2 className="text-2xl font-bold mb-6">Standings</h2>
                <div className="bg-gg-card rounded-lg p-8 text-center text-gray-400">
                    <p>No standings data available yet.</p>
                </div>
            </div>
        )
    }

    return (
        <div>
            <h2 className="text-2xl font-bold mb-6">Standings</h2>
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
                        {standings?.map((team) => (
                            <tr key={team.teamAcronym} className="hover:bg-gray-700 transition">
                                <td className="p-4 font-bold text-lg">{team.rank}</td>
                                <td className="p-4 flex items-center space-x-3">
                                    <div className="w-8 h-8 bg-gray-600 rounded-full"></div>
                                    <span className="font-bold">{team.teamName}</span>
                                </td>
                                <td className="p-4">{team.wins} - {team.losses}</td>
                                <td className="p-4 text-gg-blue">+{team.pointDiff}</td>
                                <td className="p-4">{team.winRate}</td>
                                <td className="p-4">{team.streak}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    )
}
