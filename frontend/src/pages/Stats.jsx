import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import axios from 'axios'

export default function Stats() {
    const [tab, setTab] = useState('players'); // players, teams, champions
    const [stage, setStage] = useState('lck_2025_rounds_1_2');

    const stages = [
        'lck_2025_rounds_1_2',
        'LCK 2024 Summer',
        'LCK 2024 Spring'
    ];

    return (
        <div className="flex">
            {/* Left Sidebar for Stages */}
            <div className="w-64 pr-6 border-r border-gray-700 mr-6 shrink-0">
                <h3 className="text-lg font-bold mb-4 text-gray-300 px-2">Stage</h3>
                <div className="space-y-1">
                    {stages.map(s => (
                        <button
                            key={s}
                            onClick={() => setStage(s)}
                            className={`block w-full text-left px-4 py-2 rounded transition-colors ${stage === s ? 'bg-gg-blue text-white' : 'text-gray-400 hover:bg-gray-800 hover:text-white'}`}
                        >
                            {s === 'lck_2025_rounds_1_2' && 'LCK 2025 Rounds 1-2'}
                        </button>
                    ))}
                </div>
            </div>

            <div className="flex-1 min-w-0">
                <h2 className="text-2xl font-bold mb-6">Statistics</h2>

                <div className="flex space-x-4 mb-6 border-b border-gray-700">
                    {['players', 'teams', 'champions'].map((t, i) => (
                        <button
                            key={i}
                            onClick={() => setTab(t)}
                            className={`pb-2 px-4 capitalize font-medium ${tab === t ? 'text-gg-blue border-b-2 border-gg-blue' : 'text-gray-400 hover:text-white'}`}
                        >
                            {t}
                        </button>
                    ))}
                </div>

                {tab === 'players' && <PlayerStats stage={stage} />}
                {tab === 'teams' && <TeamStats stage={stage} />}
                {tab === 'champions' && <ChampionStats stage={stage} />}
            </div>
        </div>
    )
}

function PlayerStats({ stage }) {
    const { data, isLoading } = useQuery({
        queryKey: ['stats', 'players', stage],
        queryFn: async () => (await axios.get('http://localhost:8080/api/stats/players', { params: { stage } })).data
    })

    if (isLoading) {
        return (
            <div className="bg-gg-card rounded-lg p-8 text-center text-gray-400">
                <p>Loading player stats...</p>
            </div>
        )
    }

    if (!data || data.length === 0) {
        return (
            <div className="bg-gg-card rounded-lg p-8 text-center text-gray-400">
                <p>No player stats available yet.</p>
            </div>
        )
    }

    return (
        <div className="bg-gg-card rounded-lg overflow-hidden flex flex-col">
            <div className="overflow-auto max-h-[1000px]">
                <table className="w-full text-left text-sm whitespace-nowrap">
                    <thead className="bg-gray-800 text-gray-400 uppercase sticky top-0 z-10">
                        <tr>
                            <th className="p-3 bg-gray-800">Team</th>
                            <th className="p-3 bg-gray-800">Player</th>
                            <th className="p-3 bg-gray-800">Role</th>
                            <th className="p-3 bg-gray-800">GP</th>
                            <th className="p-3 bg-gray-800">W%</th>
                            <th className="p-3 bg-gray-800">KDA</th>
                            <th className="p-3 bg-gray-800">K</th>
                            <th className="p-3 bg-gray-800">D</th>
                            <th className="p-3 bg-gray-800">A</th>
                            <th className="p-3 bg-gray-800">KP%</th>
                            <th className="p-3 bg-gray-800">KS%</th>
                            <th className="p-3 bg-gray-800">DTH%</th>
                            <th className="p-3 bg-gray-800">FB%</th>
                            <th className="p-3 bg-gray-800">GD10</th>
                            <th className="p-3 bg-gray-800">XPD10</th>
                            <th className="p-3 bg-gray-800">CSD10</th>
                            <th className="p-3 bg-gray-800">CSPM</th>
                            <th className="p-3 bg-gray-800">CS%P15</th>
                            <th className="p-3 bg-gray-800">DPM</th>
                            <th className="p-3 bg-gray-800">DMG%</th>
                            <th className="p-3 bg-gray-800">D%P15</th>
                            <th className="p-3 bg-gray-800">EGPM</th>
                            <th className="p-3 bg-gray-800">GOLD%</th>
                            <th className="p-3 bg-gray-800">WPM</th>
                            <th className="p-3 bg-gray-800">CWPM</th>
                            <th className="p-3 bg-gray-800">WCPM</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-700">
                        {data?.map((p, i) => (
                            <tr key={i} className="hover:bg-gray-700">
                                <td className="p-3 flex items-center space-x-2">
                                    {/* Team Logo */}
                                    {p.teamLogoUrl ? (
                                        <img src={p.teamLogoUrl} alt={p.teamAcronym} className="w-6 h-6 object-contain" />
                                    ) : (
                                        <div className="w-6 h-6 bg-gray-700 rounded flex items-center justify-center text-[10px] text-gray-300">
                                            {p.teamAcronym.substring(0, 2)}
                                        </div>
                                    )}

                                    {/* Team Acronym */}
                                    <span className="text-gray-400 font-normal">{p.teamAcronym}</span>
                                </td>
                                <td className="p-3 font-bold">
                                    <div className="flex items-center space-x-2">
                                        {/* Player Photo */}
                                        {p.photoUrl ? (
                                            <img src={p.photoUrl} alt={p.playerName} className="w-8 h-8 rounded-full object-cover" />
                                        ) : (
                                            <div className="w-8 h-8 bg-gray-600 rounded-full flex items-center justify-center text-xs">
                                                {p.playerName.charAt(0)}
                                            </div>
                                        )}

                                        {/* Player Handle */}
                                        <span>{p.playerName}</span>
                                    </div>
                                </td>
                                <td className="p-3">{p.role}</td>
                                <td className="p-3">{p.gamesPlayed}</td>
                                <td className="p-3">{p.winRate}%</td>
                                <td className="p-3 font-bold text-gg-blue">{p.kda}</td>
                                <td className="p-3">{p.kills}</td>
                                <td className="p-3">{p.deaths}</td>
                                <td className="p-3">{p.assists}</td>
                                <td className="p-3">{p.killParticipation}%</td>
                                <td className="p-3">{p.killShare}%</td>
                                <td className="p-3">{p.deathShare}%</td>
                                <td className="p-3">{p.firstBloodRate}%</td>
                                <td className="p-3">{p.goldDiff10}</td>
                                <td className="p-3">{p.xpDiff10}</td>
                                <td className="p-3">{p.csDiff10}</td>
                                <td className="p-3">{p.cspm}</td>
                                <td className="p-3">{p.csSharePost15}%</td>
                                <td className="p-3">{p.dpm}</td>
                                <td className="p-3">{p.damageShare}%</td>
                                <td className="p-3">{p.damageSharePost15}%</td>
                                <td className="p-3">{p.earnedGoldPerMinute}</td>
                                <td className="p-3">{p.goldShare}%</td>
                                <td className="p-3">{p.wardsPerMinute}</td>
                                <td className="p-3">{p.controlWardsPerMinute}</td>
                                <td className="p-3">{p.wardsClearedPerMinute}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    )
}

function TeamStats({ stage }) {
    const { data } = useQuery({
        queryKey: ['stats', 'teams', stage],
        queryFn: async () => (await axios.get('http://localhost:8080/api/stats/teams', { params: { stage } })).data
    })

    if (!data || data.length === 0) {
        return (
            <div className="bg-gg-card rounded-lg p-8 text-center text-gray-400">
                <p>No team stats available yet.</p>
            </div>
        )
    }

    return (
        <div className="bg-gg-card rounded-lg overflow-hidden">
            <table className="w-full text-left text-sm">
                <thead className="bg-gray-800 text-gray-400 uppercase">
                    <tr>
                        <th className="p-3">Team</th>
                        <th className="p-3">Games</th>
                        <th className="p-3">Win Rate</th>
                        <th className="p-3">KDA</th>
                        <th className="p-3">Avg Duration</th>
                        <th className="p-3">Gold/M</th>
                    </tr>
                </thead>
                <tbody className="divide-y divide-gray-700">
                    {data?.map((t, i) => (
                        <tr key={i} className="hover:bg-gray-700">
                            <td className="p-3 font-bold">{t.teamName}</td>
                            <td className="p-3">{t.gamesPlayed}</td>
                            <td className="p-3 text-gg-blue">{t.winRate}%</td>
                            <td className="p-3">{t.kda}</td>
                            <td className="p-3">{Math.floor(t.averageGameDuration / 60)}m {t.averageGameDuration % 60}s</td>
                            <td className="p-3">{t.goldPerMin}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    )
}

function ChampionStats({ stage }) {
    const { data } = useQuery({
        queryKey: ['stats', 'champions', stage],
        queryFn: async () => (await axios.get('http://localhost:8080/api/stats/champions', { params: { stage } })).data
    })

    if (!data || data.length === 0) {
        return (
            <div className="bg-gg-card rounded-lg p-8 text-center text-gray-400">
                <p>No champion stats available yet.</p>
            </div>
        )
    }

    return (
        <div className="bg-gg-card rounded-lg overflow-hidden">
            <table className="w-full text-left text-sm">
                <thead className="bg-gray-800 text-gray-400 uppercase">
                    <tr>
                        <th className="p-3">Champion</th>
                        <th className="p-3">Games</th>
                        <th className="p-3">Win Rate</th>
                        <th className="p-3">Pick Rate</th>
                        <th className="p-3">Ban Rate</th>
                        <th className="p-3">KDA</th>
                    </tr>
                </thead>
                <tbody className="divide-y divide-gray-700">
                    {data?.map((c, i) => (
                        <tr key={i} className="hover:bg-gray-700">
                            <td className="p-3 font-bold">{c.championName}</td>
                            <td className="p-3">{c.gamesPlayed}</td>
                            <td className="p-3 text-gg-blue">{c.winRate}%</td>
                            <td className="p-3">{c.pickRate}%</td>
                            <td className="p-3">{c.banRate}%</td>
                            <td className="p-3">{c.kda}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    )
}
