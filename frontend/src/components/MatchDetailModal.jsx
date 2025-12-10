import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import axios from 'axios';
import { X, Star } from 'lucide-react';

export default function MatchDetailModal({ matchId, onClose }) {
    const [activeGameIndex, setActiveGameIndex] = useState(0);
    const [isWriting, setIsWriting] = useState(false);
    const [rating, setRating] = useState(0);
    const [newComment, setNewComment] = useState('');

    const queryClient = useQueryClient();

    const { data: matchDetail, isLoading: isMatchLoading } = useQuery({
        queryKey: ['match', matchId],
        queryFn: async () => {
            const response = await axios.get(`http://localhost:8080/api/matches/${matchId}`);
            return response.data;
        },
        enabled: !!matchId
    });

    const { data: comments, isLoading: isCommentsLoading } = useQuery({
        queryKey: ['comments', matchId],
        queryFn: async () => {
            const response = await axios.get(`http://localhost:8080/api/comments/match?matchId=${matchId}`);
            return response.data;
        },
        enabled: !!matchId
    });

    const mutation = useMutation({
        mutationFn: (commentData) => {
            return axios.post(`http://localhost:8080/api/comments/match?matchId=${matchId}`, commentData);
        },
        onSuccess: () => {
            queryClient.invalidateQueries(['comments', matchId]);
            setIsWriting(false);
            setNewComment('');
            setRating(0);
        },
    });

    const handleSubmitComment = () => {
        if (rating === 0) return;
        mutation.mutate({
            comment: newComment,
            rating: rating
        });
    };

    if (!matchId) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center z-50 p-4">
            <div className="bg-gg-card w-full max-w-6xl h-[80vh] rounded-lg flex flex-col overflow-hidden relative border border-gray-700 shadow-2xl">
                <button onClick={onClose} className="absolute top-4 right-4 text-gray-400 hover:text-white z-10">
                    <X size={24} />
                </button>

                {isMatchLoading ? (
                    <div className="flex-1 flex items-center justify-center text-gray-300">Loading match details...</div>
                ) : matchDetail ? (
                    <div className="flex flex-1 overflow-hidden">
                        {/* Left Side: Game Data */}
                        <div className="w-2/3 border-r border-gray-700 flex flex-col overflow-y-auto bg-[#0f172a]">
                            {/* Game Tabs */}
                            <div className="sticky top-0 bg-[#0f172a] z-10 p-4 border-b border-gray-700 flex justify-center space-x-2">
                                {matchDetail.games.map((gameDetail, index) => (
                                    <button
                                        key={gameDetail.game.id}
                                        onClick={() => setActiveGameIndex(index)}
                                        className={`px-4 py-2 rounded font-medium transition-colors ${activeGameIndex === index ? 'bg-gg-blue text-white' : 'bg-gray-800 text-gray-400 hover:bg-gray-700 hover:text-white'}`}
                                    >
                                        Game {index + 1}
                                    </button>
                                ))}
                            </div>

                            <div className="p-6">
                                {matchDetail.games.length > 0 ? (
                                    <GameStats gameDetail={matchDetail.games[activeGameIndex]} />
                                ) : (
                                    <div className="text-center text-gray-400 mt-10">No game data available.</div>
                                )}
                            </div>
                        </div>

                        {/* Right Side: Comments */}
                        <div className="w-1/3 flex flex-col bg-gray-900">
                            <div className="p-4 border-b border-gray-700 bg-gray-800 flex justify-between items-center">
                                <h3 className="text-xl font-bold text-white">Comments</h3>
                                <button
                                    onClick={() => setIsWriting(!isWriting)}
                                    className="text-sm bg-gg-blue hover:bg-blue-600 text-white px-3 py-1 rounded transition-colors"
                                >
                                    {isWriting ? 'Cancel' : 'Add Comment'}
                                </button>
                            </div>
                            <div className="flex-1 overflow-y-auto p-4 space-y-4">
                                {isWriting && (
                                    <div className="bg-gray-800 p-4 rounded border border-gray-600 mb-4 animate-fade-in">
                                        <div className="mb-4">
                                            <label className="block text-gray-400 text-sm mb-2">Rating</label>
                                            <StarRating rating={rating} setRating={setRating} />
                                            <div className="text-right text-xs text-gray-500 mt-1">{rating}/10</div>
                                        </div>
                                        <div className="mb-4">
                                            <label className="block text-gray-400 text-sm mb-2">Comment</label>
                                            <textarea
                                                className="w-full bg-gray-900 border border-gray-700 rounded p-2 text-white focus:outline-none focus:border-gg-blue resize-none"
                                                rows="3"
                                                value={newComment}
                                                onChange={(e) => setNewComment(e.target.value)}
                                                placeholder="Share your thoughts..."
                                            />
                                        </div>
                                        <button
                                            onClick={handleSubmitComment}
                                            disabled={mutation.isPending || rating === 0}
                                            className="w-full bg-gg-blue hover:bg-blue-600 text-white py-2 rounded font-bold transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                                        >
                                            {mutation.isPending ? 'Posting...' : 'Post Comment'}
                                        </button>
                                    </div>
                                )}

                                {isCommentsLoading ? (
                                    <p className="text-gray-400">Loading comments...</p>
                                ) : comments && comments.length > 0 ? (
                                    comments.map((comment, idx) => (
                                        <div key={idx} className="bg-gray-800 p-4 rounded border border-gray-700">
                                            <div className="flex justify-between mb-2 items-center">
                                                <div className="flex items-center space-x-1">
                                                    <Star size={16} className="text-yellow-500 fill-yellow-500" />
                                                    <span className="font-bold text-yellow-500">{comment.rating}/10</span>
                                                </div>
                                            </div>
                                            <p className="text-gray-300 text-sm whitespace-pre-wrap">{comment.comment}</p>
                                        </div>
                                    ))
                                ) : (
                                    <p className="text-gray-500 text-center mt-4">No comments yet. Be the first!</p>
                                )}
                            </div>
                        </div>
                    </div>
                ) : (
                    <div className="flex-1 flex items-center justify-center text-red-400">Failed to load match details.</div>
                )}
            </div>
        </div>
    );
}

function StarRating({ rating, setRating }) {
    const [hoverRating, setHoverRating] = useState(0);

    const handleMouseMove = (e, starIndex) => {
        const rect = e.currentTarget.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const isHalf = x < rect.width / 2;
        setHoverRating((starIndex - 1) * 2 + (isHalf ? 1 : 2));
    };

    const handleClick = () => {
        setRating(hoverRating);
    };

    const displayRating = hoverRating || rating;

    return (
        <div className="flex space-x-1" onMouseLeave={() => setHoverRating(0)}>
            {[1, 2, 3, 4, 5].map((star) => {
                const value = star * 2;
                const isFull = displayRating >= value;
                const isHalf = displayRating >= value - 1 && !isFull;

                return (
                    <div
                        key={star}
                        className="cursor-pointer relative"
                        onMouseMove={(e) => handleMouseMove(e, star)}
                        onClick={handleClick}
                    >
                        {/* Background Star (Empty) */}
                        <Star size={24} className="text-gray-600" />

                        {/* Foreground Star (Filled) - Clipped */}
                        <div
                            className="absolute top-0 left-0 overflow-hidden pointer-events-none"
                            style={{ width: isFull ? '100%' : isHalf ? '50%' : '0%' }}
                        >
                            <Star size={24} className="text-yellow-500 fill-yellow-500" />
                        </div>
                    </div>
                );
            })}
        </div>
    );
}

function GameStats({ gameDetail }) {
    const { teamGameStats, playerGameStats } = gameDetail;

    const blueTeam = teamGameStats.find(t => t.side === 'Blue');
    const redTeam = teamGameStats.find(t => t.side === 'Red');

    const bluePlayers = playerGameStats.filter(p => p.side === 'Blue');
    const redPlayers = playerGameStats.filter(p => p.side === 'Red');

    // Sort players by role to ensure correct comparison
    const roleOrder = { 'TOP': 1, 'JUNGLE': 2, 'MID': 3, 'BOT': 4, 'SUPPORT': 5 };
    const getRoleOrder = (p) => roleOrder[p.position] || 99;

    bluePlayers.sort((a, b) => getRoleOrder(a) - getRoleOrder(b));
    redPlayers.sort((a, b) => getRoleOrder(a) - getRoleOrder(b));

    if (!blueTeam || !redTeam) return <div className="text-center text-gray-400">Incomplete game data.</div>;

    return (
        <div className="flex flex-col h-full">
            {/* Row 1: Team KDA Comparison */}
            <div className="grid grid-cols-3 items-center py-4 border-b border-gray-700 bg-gray-800/20 rounded-t-lg">
                <div className="text-right pr-4">
                    <div className="text-2xl font-bold text-blue-400">{blueTeam.team.name}</div>
                    <div className="font-mono text-lg text-gray-300">
                        <span className="text-green-400">{blueTeam.kills}</span> / <span className="text-red-400">{blueTeam.deaths}</span> / <span className="text-yellow-400">{blueTeam.assists}</span>
                    </div>
                </div>
                <div className="text-center font-bold text-gray-600 text-xl">VS</div>
                <div className="text-left pl-4">
                    <div className="text-2xl font-bold text-red-400">{redTeam.team.name}</div>
                    <div className="font-mono text-lg text-gray-300">
                        <span className="text-green-400">{redTeam.kills}</span> / <span className="text-red-400">{redTeam.deaths}</span> / <span className="text-yellow-400">{redTeam.assists}</span>
                    </div>
                </div>
            </div>

            {/* Row 2: Bans Comparison */}
            <div className="grid grid-cols-2 gap-4 py-3 border-b border-gray-700 bg-gray-800/40">
                <div className="flex justify-end items-center space-x-2 pr-4">
                    <span className="text-gray-500 text-xs font-bold uppercase tracking-wider mr-2">Bans</span>
                    {[blueTeam.ban1, blueTeam.ban2, blueTeam.ban3, blueTeam.ban4, blueTeam.ban5].map((ban, i) => (
                        <span key={i} className="text-xs bg-gray-900 px-2 py-1 rounded text-gray-400 border border-gray-700">{ban || '-'}</span>
                    ))}
                </div>
                <div className="flex justify-start items-center space-x-2 pl-4">
                    {[redTeam.ban1, redTeam.ban2, redTeam.ban3, redTeam.ban4, redTeam.ban5].map((ban, i) => (
                        <span key={i} className="text-xs bg-gray-900 px-2 py-1 rounded text-gray-400 border border-gray-700">{ban || '-'}</span>
                    ))}
                    <span className="text-gray-500 text-xs font-bold uppercase tracking-wider ml-2">Bans</span>
                </div>
            </div>

            {/* Rows 3-7: Player Comparison */}
            <div className="flex-1 overflow-auto mt-2">
                <table className="w-full text-sm border-collapse">
                    <thead className="text-gray-500 border-b border-gray-700 bg-gray-800/50">
                        <tr>
                            <th className="py-2 text-right w-[15%] pr-2">Blue Player</th>
                            <th className="py-2 text-center w-[10%]">Champ</th>
                            <th className="py-2 text-center w-[20%]">KDA / Gold / Dmg</th>
                            <th className="py-2 text-center w-[10%]">Role</th>
                            <th className="py-2 text-center w-[20%]">KDA / Gold / Dmg</th>
                            <th className="py-2 text-center w-[10%]">Champ</th>
                            <th className="py-2 text-left w-[15%] pl-2">Red Player</th>
                        </tr>
                    </thead>
                    <tbody>
                        {bluePlayers.map((bp, i) => {
                            const rp = redPlayers[i] || {};
                            return (
                                <tr key={bp.id} className="border-b border-gray-700/30 hover:bg-gray-800/20 transition-colors">
                                    {/* Blue Side */}
                                    <td className="py-3 text-right font-medium text-blue-300 pr-2">{bp.player.handle}</td>
                                    <td className="py-3 text-center text-gray-400">{bp.champion.name}</td>
                                    <td className="py-3 text-center text-xs space-y-0.5">
                                        <div className="font-mono text-white">{bp.kills}/{bp.deaths}/{bp.assists}</div>
                                        <div className="text-yellow-600">{bp.totalGold?.toLocaleString()} <span className="text-gray-600">G</span></div>
                                        <div className="text-gray-500">{bp.damageToChampions?.toLocaleString()} <span className="text-gray-600">Dmg</span></div>
                                    </td>

                                    {/* Role */}
                                    <td className="py-3 text-center font-bold text-gray-700 uppercase text-xs tracking-widest">{bp.position}</td>

                                    {/* Red Side */}
                                    <td className="py-3 text-center text-xs space-y-0.5">
                                        <div className="font-mono text-white">{rp.kills}/{rp.deaths}/{rp.assists}</div>
                                        <div className="text-yellow-600">{rp.totalGold?.toLocaleString()} <span className="text-gray-600">G</span></div>
                                        <div className="text-gray-500">{rp.damageToChampions?.toLocaleString()} <span className="text-gray-600">Dmg</span></div>
                                    </td>
                                    <td className="py-3 text-center text-gray-400">{rp.champion?.name}</td>
                                    <td className="py-3 text-left font-medium text-red-300 pl-2">{rp.player?.handle}</td>
                                </tr>
                            );
                        })}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

