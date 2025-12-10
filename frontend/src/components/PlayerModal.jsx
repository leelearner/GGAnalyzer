import React, { useMemo } from 'react';
import { Radar, RadarChart, PolarGrid, PolarAngleAxis, PolarRadiusAxis, ResponsiveContainer, Tooltip } from 'recharts';
import { X } from 'lucide-react';

export default function PlayerModal({ player, allPlayers, onClose }) {
    if (!player) return null;

    const data = useMemo(() => {
        if (!allPlayers || allPlayers.length === 0) return [];

        // Normalize role for comparison (handle potential case differences)
        const currentRole = player.role.toLowerCase();

        // Filter players strictly by the same role to ensure separate calculation
        const rolePlayers = allPlayers.filter(p => p.role.toLowerCase() === currentRole);

        // Define metrics for each role separately
        const supportMetrics = [
            { key: 'kda', label: 'KDA', format: v => v.toFixed(2) },
            { key: 'winRate', label: 'W%', format: v => `${v.toFixed(0)}%` },
            { key: 'killParticipation', label: 'KP%', format: v => `${v.toFixed(1)}%` },
            { key: 'dpm', label: 'DPM', format: v => v.toFixed(0) },
            { key: 'damageShare', label: 'DMG%', format: v => `${v.toFixed(1)}%` },
            { key: 'wardsPerMinute', label: 'WPM', format: v => v.toFixed(2) },
            { key: 'controlWardsPerMinute', label: 'CWPM', format: v => v.toFixed(2) },
            { key: 'wardsClearedPerMinute', label: 'WCPM', format: v => v.toFixed(2) },
        ];

        const topMetrics = [
            { key: 'kda', label: 'KDA', format: v => v.toFixed(2) },
            { key: 'winRate', label: 'W%', format: v => `${v.toFixed(0)}%` },
            { key: 'dpm', label: 'DPM', format: v => v.toFixed(0) },
            { key: 'damageShare', label: 'DMG%', format: v => `${v.toFixed(1)}%` },
            { key: 'cspm', label: 'CSPM', format: v => v.toFixed(1) },
            { key: 'goldShare', label: 'GOLD%', format: v => `${v.toFixed(1)}%` },
            {
                key: 'damageConversion',
                label: 'D%/G%',
                format: v => `${v.toFixed(0)}%`,
                getValue: (p) => p.goldShare > 0 ? (p.damageShare / p.goldShare) * 100 : 0
            },
        ];

        const jungleMetrics = [
            { key: 'kda', label: 'KDA', format: v => v.toFixed(2) },
            { key: 'winRate', label: 'W%', format: v => `${v.toFixed(0)}%` },
            { key: 'dpm', label: 'DPM', format: v => v.toFixed(0) },
            { key: 'damageShare', label: 'DMG%', format: v => `${v.toFixed(1)}%` },
            { key: 'cspm', label: 'CSPM', format: v => v.toFixed(1) },
            { key: 'goldShare', label: 'GOLD%', format: v => `${v.toFixed(1)}%` },
            {
                key: 'damageConversion',
                label: 'D%/G%',
                format: v => `${v.toFixed(0)}%`,
                getValue: (p) => p.goldShare > 0 ? (p.damageShare / p.goldShare) * 100 : 0
            },
        ];

        const midMetrics = [
            { key: 'kda', label: 'KDA', format: v => v.toFixed(2) },
            { key: 'winRate', label: 'W%', format: v => `${v.toFixed(0)}%` },
            { key: 'dpm', label: 'DPM', format: v => v.toFixed(0) },
            { key: 'damageShare', label: 'DMG%', format: v => `${v.toFixed(1)}%` },
            { key: 'cspm', label: 'CSPM', format: v => v.toFixed(1) },
            { key: 'goldShare', label: 'GOLD%', format: v => `${v.toFixed(1)}%` },
            {
                key: 'damageConversion',
                label: 'D%/G%',
                format: v => `${v.toFixed(0)}%`,
                getValue: (p) => p.goldShare > 0 ? (p.damageShare / p.goldShare) * 100 : 0
            },
        ];

        const botMetrics = [
            { key: 'kda', label: 'KDA', format: v => v.toFixed(2) },
            { key: 'winRate', label: 'W%', format: v => `${v.toFixed(0)}%` },
            { key: 'dpm', label: 'DPM', format: v => v.toFixed(0) },
            { key: 'damageShare', label: 'DMG%', format: v => `${v.toFixed(1)}%` },
            { key: 'cspm', label: 'CSPM', format: v => v.toFixed(1) },
            { key: 'goldShare', label: 'GOLD%', format: v => `${v.toFixed(1)}%` },
            {
                key: 'damageConversion',
                label: 'D%/G%',
                format: v => `${v.toFixed(0)}%`,
                getValue: (p) => p.goldShare > 0 ? (p.damageShare / p.goldShare) * 100 : 0
            },
        ];

        // Explicitly map each role to its metrics configuration
        let metrics = [];
        if (['support', 'sup'].includes(currentRole)) {
            metrics = supportMetrics;
        } else if (['top'].includes(currentRole)) {
            metrics = topMetrics;
        } else if (['jungle', 'jng', 'jug'].includes(currentRole)) {
            metrics = jungleMetrics;
        } else if (['mid', 'middle'].includes(currentRole)) {
            metrics = midMetrics;
        } else if (['bot', 'adc', 'bottom'].includes(currentRole)) {
            metrics = botMetrics;
        } else {
            // Fallback
            metrics = midMetrics;
        }

        return metrics.map(metric => {
            const getValue = metric.getValue || ((p) => p[metric.key]);

            // Calculate min/max specifically for this role group
            const values = rolePlayers.map(p => getValue(p));
            const min = Math.min(...values);
            const max = Math.max(...values);
            const value = getValue(player);

            // Avoid division by zero if max === min
            let normalized = 0;
            if (max > min) {
                normalized = ((value - min) / (max - min)) * 100;
            } else {
                normalized = 100;
            }

            return {
                subject: metric.label,
                A: normalized,
                fullMark: 100,
                value: metric.format(value),
                min: metric.format(min),
                max: metric.format(max)
            };
        });
    }, [player, allPlayers]); return (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4 backdrop-blur-sm" onClick={onClose}>
            <div className="bg-gray-900 rounded-lg w-full max-w-3xl p-6 relative border border-gray-700 shadow-2xl" onClick={e => e.stopPropagation()}>
                <button onClick={onClose} className="absolute top-4 right-4 text-gray-400 hover:text-white transition-colors">
                    <X size={24} />
                </button>

                <div className="flex flex-col md:flex-row gap-8">
                    {/* Player Info */}
                    <div className="flex-1 flex flex-col items-center justify-center text-center">
                        <div className="w-32 h-32 rounded-full overflow-hidden bg-gray-800 mb-4 border-4 border-cyan-500 shadow-lg">
                            {player.photoUrl ? (
                                <img src={player.photoUrl} alt={player.playerName} className="w-full h-full object-cover" />
                            ) : (
                                <div className="w-full h-full flex items-center justify-center text-4xl text-gray-500 font-bold">
                                    {player.playerName.charAt(0)}
                                </div>
                            )}
                        </div>
                        <h2 className="text-3xl font-bold text-white mb-1">{player.playerName}</h2>
                        <div className="flex items-center gap-2 text-gray-400 text-lg mb-6">
                            {player.teamLogoUrl && <img src={player.teamLogoUrl} alt={player.teamAcronym} className="w-6 h-6 object-contain" />}
                            <span className="font-semibold">{player.teamAcronym}</span>
                            <span>•</span>
                            <span>{player.role}</span>
                        </div>

                        <div className="grid grid-cols-2 gap-4 w-full">
                            <div className="bg-gray-800/50 p-4 rounded-lg border border-gray-700">
                                <div className="text-gray-400 text-sm uppercase tracking-wider">Games</div>
                                <div className="text-2xl font-bold text-white">{player.gamesPlayed}</div>
                            </div>
                            <div className="bg-gray-800/50 p-4 rounded-lg border border-gray-700">
                                <div className="text-gray-400 text-sm uppercase tracking-wider">Win Rate</div>
                                <div className="text-2xl font-bold text-cyan-400">{player.winRate.toFixed(0)}%</div>
                            </div>
                        </div>
                    </div>

                    {/* Radar Chart */}
                    <div className="flex-1 h-[350px] min-w-[300px] bg-gray-800/30 rounded-xl p-2">
                        <ResponsiveContainer width="100%" height="100%">
                            <RadarChart cx="50%" cy="50%" outerRadius="70%" data={data}>
                                <PolarGrid stroke="#374151" />
                                <PolarAngleAxis dataKey="subject" tick={{ fill: '#9CA3AF', fontSize: 12, fontWeight: 600 }} />
                                <PolarRadiusAxis angle={30} domain={[0, 100]} tick={false} axisLine={false} />
                                <Radar
                                    name={player.playerName}
                                    dataKey="A"
                                    stroke="#06b6d4"
                                    strokeWidth={3}
                                    fill="#06b6d4"
                                    fillOpacity={0.3}
                                />
                                <Tooltip
                                    content={({ active, payload }) => {
                                        if (active && payload && payload.length) {
                                            const data = payload[0].payload;
                                            return (
                                                <div className="bg-gray-900 border border-gray-700 p-3 rounded shadow-xl">
                                                    <p className="text-gray-400 text-xs uppercase font-bold mb-1">{data.subject}</p>
                                                    <p className="text-cyan-400 text-lg font-bold">{data.value}</p>
                                                    <div className="mt-2 pt-2 border-t border-gray-700 text-xs text-gray-500 flex justify-between gap-4">
                                                        <span>Min: {data.min}</span>
                                                        <span>Max: {data.max}</span>
                                                    </div>
                                                </div>
                                            );
                                        }
                                        return null;
                                    }}
                                />
                            </RadarChart>
                        </ResponsiveContainer>
                    </div>
                </div>
            </div>
        </div>
    );
}
