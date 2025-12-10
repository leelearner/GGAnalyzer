import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import axios from 'axios'
import { STAT_DEFINITIONS } from '../constants/definitions'
import PlayerModal from '../components/PlayerModal'

export default function Stats() {
    const [tab, setTab] = useState('players'); // players, teams, champions
    const [stage, setStage] = useState('lck_2025_rounds_1_2');

    const { data: stagesData } = useQuery({
        queryKey: ['stages'],
        queryFn: async () => {
            const response = await axios.get('http://localhost:8080/api/stages')
            return response.data
        }
    })

    const stages = stagesData ? stagesData.map(s => s.name).sort() : [];

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
                            {s.replace(/_/g, ' ')
                                .replace(/^(\w+)/, c => c.toUpperCase())
                                .replace(/\b(\w)/g, c => c.toUpperCase())
                                .replace(/(\d)\s+(\d)/g, '$1-$2')}
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
    const [selectedPlayer, setSelectedPlayer] = useState(null);
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
            <div className="overflow-auto max-h-[600px]">
                <table className="w-full text-left text-sm whitespace-nowrap">
                    <thead className="bg-gray-800 text-gray-400 uppercase sticky top-0 z-10">
                        <tr>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["Team"]}>Team</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["Player"]}>Player</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["Pos"]}>Role</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["GP"]}>GP</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["W%"]}>W%</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["KDA"]}>KDA</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["K"]}>K</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["D"]}>D</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["A"]}>A</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["KP%"]}>KP%</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["KS%"]}>KS%</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["DTH%"]}>DTH%</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["FB%"]}>FB%</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["GD10"]}>GD10</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["XPD10"]}>XPD10</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["CSD10"]}>CSD10</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["CSPM"]}>CSPM</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["CS%P15"]}>CS%P15</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["DPM"]}>DPM</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["DMG%"]}>DMG%</th>
                            {/* <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["D%P15"]}>D%P15</th> */}
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["EGPM"]}>EGPM</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["GOLD%"]}>GOLD%</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["WPM"]}>WPM</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["CWPM"]}>CWPM</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["WCPM"]}>WCPM</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-700">
                        {data?.map((p, i) => (
                            <tr
                                key={i}
                                className="hover:bg-gray-700 cursor-pointer transition-colors"
                                onClick={() => setSelectedPlayer(p)}
                            >
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
                                <td className="p-3">{p.winRate.toFixed(0)}%</td>
                                <td className="p-3 font-bold text-gg-blue">{p.kda.toFixed(1)}</td>
                                <td className="p-3">{p.kills}</td>
                                <td className="p-3">{p.deaths}</td>
                                <td className="p-3">{p.assists}</td>
                                <td className="p-3">{p.killParticipation.toFixed(1)}%</td>
                                <td className="p-3">{p.killShare.toFixed(1)}%</td>
                                <td className="p-3">{p.deathShare.toFixed(1)}%</td>
                                <td className="p-3">{p.firstBloodRate.toFixed(0)}%</td>
                                <td className="p-3">{p.goldDiff10.toFixed(0)}</td>
                                <td className="p-3">{p.xpDiff10.toFixed(0)}</td>
                                <td className="p-3">{p.csDiff10.toFixed(1)}</td>
                                <td className="p-3">{p.cspm.toFixed(1)}</td>
                                <td className="p-3">{p.csSharePost15.toFixed(1)}%</td>
                                <td className="p-3">{p.dpm.toFixed(0)}</td>
                                <td className="p-3">{p.damageShare.toFixed(0)}%</td>
                                {/* <td className="p-3">{p.damageSharePost15.toFixed(0)}%</td> */}
                                <td className="p-3">{p.earnedGoldPerMinute.toFixed(0)}</td>
                                <td className="p-3">{p.goldShare.toFixed(1)}%</td>
                                <td className="p-3">{p.wardsPerMinute.toFixed(2)}</td>
                                <td className="p-3">{p.controlWardsPerMinute.toFixed(2)}</td>
                                <td className="p-3">{p.wardsClearedPerMinute.toFixed(2)}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
            {selectedPlayer && (
                <PlayerModal
                    player={selectedPlayer}
                    allPlayers={data}
                    onClose={() => setSelectedPlayer(null)}
                />
            )}
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
            <div className="overflow-auto max-h-[600px]">
                <table className="w-full text-left text-sm whitespace-nowrap">
                    <thead className="bg-gray-800 text-gray-400 uppercase sticky top-0 z-10">
                        <tr>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["Team"]}>Team</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["GP"]}>GP</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["W%"]}>W%</th>
                            <th className="p-3 bg-gray-800" title="Kill to Death Ratio">K:D</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["AGT"]}>AGT</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["CKPM"]}>CKPM</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["GPR"]}>GPR</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["GSPD"]}>GSPD</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["GD15"]}>GD15</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["FB%"]}>FB%</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["FT%"]}>FT%</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["F3T%"]}>F3T%</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["PPG"]}>PPG</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["HLD%"]}>HLD%</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["GRB%"]}>GRB%</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["FD%"]}>FD%</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["DRG%"]}>DRG%</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["ELD%"]}>ELD%</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["FBN%"]}>FBN%</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["BN%"]}>BN%</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["LNE%"]}>LNE%</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["JNG%"]}>JNG%</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["WPM"]}>WPM</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["CWPM"]}>CWPM</th>
                            <th className="p-3 bg-gray-800" title={STAT_DEFINITIONS["WCPM"]}>WCPM</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-gray-700">
                        {data?.map((t, i) => (
                            <tr key={i} className="hover:bg-gray-700">
                                <td className="p-3 font-bold">{t.team.acronym}</td>
                                <td className="p-3">{t.gamesPlayed}</td>
                                <td className="p-3 text-gg-blue">{t.gamesPlayed > 0 ? ((t.wins / t.gamesPlayed) * 100).toFixed(0) : 0}%</td>
                                <td className="p-3">{t.kd?.toFixed(2)}</td>
                                <td className="p-3">{t.averageGameTime?.toFixed(1)}</td>
                                <td className="p-3">{t.combinedKillsPerMinute?.toFixed(2)}</td>
                                <td className="p-3">{t.goldPercentRating?.toFixed(1)}</td>
                                <td className="p-3">{t.goldSpentPerDiff?.toFixed(1)}%</td>
                                <td className="p-3">{t.gd15?.toFixed(0)}</td>
                                <td className="p-3">{t.firstBloodPercent?.toFixed(0)}%</td>
                                <td className="p-3">{t.firstTowerPercent?.toFixed(0)}%</td>
                                <td className="p-3">{t.firstThreeTowersPercent?.toFixed(0)}%</td>
                                <td className="p-3">{t.ppg?.toFixed(1)}</td>
                                <td className="p-3">{t.heraldPercent?.toFixed(0)}%</td>
                                <td className="p-3">{t.grubPercent?.toFixed(0)}%</td>
                                <td className="p-3">{t.firstDragonPercent?.toFixed(0)}%</td>
                                <td className="p-3">{t.dragonPercent?.toFixed(0)}%</td>
                                <td className="p-3">{t.elderDragonPercent?.toFixed(0)}%</td>
                                <td className="p-3">{t.firstBaronPercent?.toFixed(0)}%</td>
                                <td className="p-3">{t.baronPercent?.toFixed(0)}%</td>
                                <td className="p-3">{t.lanePercent?.toFixed(1)}%</td>
                                <td className="p-3">{t.junglePercent?.toFixed(1)}%</td>
                                <td className="p-3">{t.wardsPerMinute?.toFixed(2)}</td>
                                <td className="p-3">{t.controlWardsPerMinute?.toFixed(2)}</td>
                                <td className="p-3">{t.wardsClearedPerMinute?.toFixed(2)}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
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
                        <th className="p-3" title={STAT_DEFINITIONS["Champion"]}>Champion</th>
                        <th className="p-3" title={STAT_DEFINITIONS["GP"]}>Games</th>
                        <th className="p-3" title={STAT_DEFINITIONS["W%"]}>Win Rate</th>
                        <th className="p-3" title={STAT_DEFINITIONS["P%"]}>Pick Rate</th>
                        <th className="p-3" title={STAT_DEFINITIONS["B%"]}>Ban Rate</th>
                        <th className="p-3" title={STAT_DEFINITIONS["KDA"]}>KDA</th>
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
