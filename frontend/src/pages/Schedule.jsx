import { useState, useEffect, useRef } from 'react'
import { useQuery } from '@tanstack/react-query'
import axios from 'axios'
import { Calendar } from 'lucide-react'
import MatchDetailModal from '../components/MatchDetailModal'

export default function Schedule() {
    const [selectedStage, setSelectedStage] = useState(null);
    const [selectedMatchId, setSelectedMatchId] = useState(null);
    const dateRefs = useRef({});

    const { data: matches, isLoading, error } = useQuery({
        queryKey: ['matches'],
        queryFn: async () => {
            const response = await axios.get('http://localhost:8080/api/matches')
            return response.data
        }
    })

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

    // Filter matches by stage
    const filteredMatches = matches
        ? matches.filter(m => m.stage?.name === selectedStage)
        : [];

    // Group by date
    const matchesByDate = filteredMatches.reduce((acc, match) => {
        const dateKey = new Date(match.startTime).toISOString().split('T')[0];
        if (!acc[dateKey]) {
            acc[dateKey] = [];
        }
        acc[dateKey].push(match);
        return acc;
    }, {});

    // Sort dates
    const sortedDates = Object.keys(matchesByDate).sort();

    const scrollToDate = (dateKey) => {
        dateRefs.current[dateKey]?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    };

    const formatDateDisplay = (dateStr) => {
        const date = new Date(dateStr);
        return date.toLocaleDateString(undefined, { month: 'short', day: 'numeric', weekday: 'short' });
    };

    if (isLoading) return <p>Loading matches...</p>;
    if (error) return <p className="text-red-500">Error loading matches. Is the backend running?</p>;

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
                <h2 className="text-2xl font-bold mb-6">Match Schedule</h2>

                {/* Date Navigation */}
                {sortedDates.length > 0 && (
                    <div className="flex space-x-2 overflow-x-auto pb-4 mb-4 scrollbar-hide">
                        {sortedDates.map(dateKey => (
                            <button
                                key={dateKey}
                                onClick={() => scrollToDate(dateKey)}
                                className="px-4 py-2 bg-gray-800 hover:bg-gray-700 rounded whitespace-nowrap text-sm font-medium transition-colors"
                            >
                                {formatDateDisplay(dateKey)}
                            </button>
                        ))}
                    </div>
                )}

                {/* Matches List */}
                <div className="flex-1 overflow-y-auto pr-2">
                    {sortedDates.length === 0 && (
                        <div className="bg-gg-card rounded-lg p-8 text-center text-gray-400">
                            <p>No matches scheduled for this stage.</p>
                        </div>
                    )}

                    <div className="space-y-8">
                        {sortedDates.map(dateKey => (
                            <div key={dateKey} ref={el => dateRefs.current[dateKey] = el}>
                                <h3 className="text-lg font-bold text-gray-300 mb-3 sticky top-0 bg-[#0f172a] py-2 z-10">
                                    {formatDateDisplay(dateKey)}
                                </h3>
                                <div className="grid gap-4">
                                    {matchesByDate[dateKey].map(match => (
                                        <div
                                            key={match.id}
                                            className="bg-gg-card p-4 rounded-lg flex justify-between items-center hover:bg-gray-700 transition cursor-pointer"
                                            onClick={() => setSelectedMatchId(match.matchId)}
                                        >
                                            <div className="flex items-center space-x-4 w-1/3">
                                                <span className="text-gray-400 text-sm">{new Date(match.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                                                <span className="font-bold">{match.league?.name}</span>
                                            </div>

                                            <div className="flex items-center justify-center space-x-8 w-1/3">
                                                <div className="flex items-center space-x-2 justify-end w-24">
                                                    <span className="font-bold text-lg">{match.teamA?.acronym}</span>
                                                    {match.teamA?.logoUrl ? (
                                                        <img src={match.teamA.logoUrl} alt={match.teamA.acronym} className="w-8 h-8 rounded-full" />
                                                    ) : (
                                                        <div className="w-8 h-8 bg-gray-600 rounded-full"></div>
                                                    )}
                                                </div>
                                                <div className="text-xl font-bold text-gray-300 whitespace-nowrap">
                                                    {match.teamAScore} - {match.teamBScore}
                                                </div>
                                                <div className="flex items-center space-x-2 w-24">
                                                    {match.teamB?.logoUrl ? (
                                                        <img src={match.teamB.logoUrl} alt={match.teamB.acronym} className="w-8 h-8 rounded-full" />
                                                    ) : (
                                                        <div className="w-8 h-8 bg-gray-600 rounded-full"></div>
                                                    )}
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
                        ))}
                    </div>
                </div>
            </div>
            {selectedMatchId && (
                <MatchDetailModal matchId={selectedMatchId} onClose={() => setSelectedMatchId(null)} />
            )}
        </div>
    )
}
