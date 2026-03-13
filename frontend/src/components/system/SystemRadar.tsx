import { motion } from 'framer-motion';

export interface SystemRadarProps {
    data: { stat: string; value: number; max: number }[];
    size?: number;
}

export function SystemRadar({ data, size = 200 }: SystemRadarProps) {
    const center = size / 2;
    const radius = size / 2 - 20;
    const angleStep = (Math.PI * 2) / data.length;

    // Calculate Polygon points
    const points = data.map((d, i) => {
        const ratio = Math.max(0.1, d.value / d.max); // Ensure it doesn't collapse entirely
        const angle = i * angleStep - Math.PI / 2;
        const x = center + radius * ratio * Math.cos(angle);
        const y = center + radius * ratio * Math.sin(angle);
        return `${x},${y}`;
    }).join(' ');

    return (
        <motion.div
            initial={{ scale: 0, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ type: "spring", stiffness: 100, damping: 10 }}
            className="relative"
            style={{ width: size, height: size }}
        >
            <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`}>
                {/* Background Web */}
                {[0.25, 0.5, 0.75, 1].map((scale, index) => {
                    const webPoints = data.map((_, i) => {
                        const angle = i * angleStep - Math.PI / 2;
                        const x = center + radius * scale * Math.cos(angle);
                        const y = center + radius * scale * Math.sin(angle);
                        return `${x},${y}`;
                    }).join(' ');

                    return (
                        <polygon
                            key={`web-${index}`}
                            points={webPoints}
                            fill="none"
                            stroke="rgba(6, 182, 212, 0.15)"
                            strokeWidth="1"
                        />
                    );
                })}

                {/* Plot Area */}
                <motion.polygon
                    initial={{ pathLength: 0 }}
                    animate={{ pathLength: 1 }}
                    transition={{ duration: 0.5, delay: 0.1 }}
                    points={points}
                    fill="rgba(6, 182, 212, 0.3)"
                    stroke="rgba(6, 182, 212, 1)"
                    strokeWidth="2"
                    style={{ filter: "drop-shadow(0px 0px 4px rgba(6, 182, 212, 0.8))" }}
                />

                {/* Connectors */}
                {data.map((_, i) => {
                    const angle = i * angleStep - Math.PI / 2;
                    const x = center + radius * Math.cos(angle);
                    const y = center + radius * Math.sin(angle);
                    return (
                        <line
                            key={`connector-${i}`}
                            x1={center}
                            y1={center}
                            x2={x}
                            y2={y}
                            stroke="rgba(6, 182, 212, 0.2)"
                            strokeWidth="1"
                        />
                    );
                })}

                {/* Labels */}
                {data.map((d, i) => {
                    const angle = i * angleStep - Math.PI / 2;
                    const labelRadius = radius + 15;
                    const x = center + labelRadius * Math.cos(angle);
                    const y = center + labelRadius * Math.sin(angle);
                    return (
                        <text
                            key={`label-${i}`}
                            x={x}
                            y={y}
                            fill="#9ca3af"
                            fontSize="10"
                            fontFamily="monospace"
                            textAnchor="middle"
                            dominantBaseline="middle"
                        >
                            {d.stat}
                        </text>
                    );
                })}
            </svg>
        </motion.div>
    );
}
