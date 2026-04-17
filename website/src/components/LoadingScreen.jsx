import React, { useEffect, useState } from 'react';
import { useProgress } from '@react-three/drei';
import { gsap } from 'gsap';

const LoadingScreen = ({ onStarted }) => {
  const { progress } = useProgress();
  const [showButton, setShowButton] = useState(false);

  useEffect(() => {
    // Fail-safe: Force show button after 3 seconds regardless of progress
    const timer = setTimeout(() => {
      setShowButton(true);
      console.log('Master Logic: Loading fail-safe triggered.');
    }, 3000);

    if (progress >= 100) {
      setShowButton(true);
      clearTimeout(timer);
    }
    
    return () => clearTimeout(timer);
  }, [progress]);

  const handleStart = () => {
    gsap.to('.loader-container', {
      opacity: 0,
      scale: 1.5,
      duration: 1,
      ease: 'power4.inOut',
      onComplete: onStarted
    });
  };

  // Diagnostic lines
  const diagnostics = [
    'INITIALIZING VOXEL ENGINE...',
    'SYNCING MULTIVERSE SHADERS...',
    'CALIBRATING WORLD GENERATORS...',
    'LINKING NEURAL MESH...',
    'ESTABLISHING MASTER CONNECTION...'
  ];

  return (
    <div className="loader-container fixed inset-0 z-[100] flex flex-col items-center justify-center bg-[#050505] text-white font-mono overflow-hidden">
      {/* Background Grid */}
      <div className="absolute inset-0 opacity-10 pointer-events-none" 
           style={{ backgroundImage: 'radial-gradient(circle, #00f7ff 1px, transparent 1px)', backgroundSize: '40px 40px' }} />

      <div className="relative w-64 h-64 mb-12">
        {/* Logo Placeholder / Animated Ring */}
        <svg className="w-full h-full transform -rotate-90">
          <circle
            cx="128"
            cy="128"
            r="120"
            stroke="currentColor"
            strokeWidth="2"
            fill="transparent"
            className="text-white/10"
          />
          <circle
            cx="128"
            cy="128"
            r="120"
            stroke="currentColor"
            strokeWidth="4"
            fill="transparent"
            strokeDasharray={754}
            strokeDashoffset={754 - (754 * progress) / 100}
            className="text-cyan transition-all duration-300 ease-out"
            strokeLinecap="round"
          />
        </svg>
        <div className="absolute inset-0 flex flex-col items-center justify-center">
          <img src="logo.png" alt="Logo" className="w-32 animate-pulse" />
          <span className="text-2xl font-bold mt-4 font-outfit tracking-widest">{Math.round(progress)}%</span>
        </div>
      </div>

      <div className="w-full max-w-md h-24 overflow-hidden mb-12 flex flex-col items-center">
        {diagnostics.map((line, i) => (
          <div key={i} className="text-cyan/60 text-xs mb-1 animate-pulse" style={{ animationDelay: `${i * 0.2}s` }}>
            {line}
          </div>
        ))}
      </div>

      {showButton && (
        <button
          onClick={handleStart}
          className="px-12 py-4 glass border-cyan/50 text-cyan font-bold rounded-lg hover:cyan-glow transition-all animate-bounce"
        >
          BEGIN MISSION
        </button>
      )}

      <div className="absolute bottom-8 text-white/20 text-[10px] tracking-widest">
        SYSTEM READY // WYNO-OS CORE 5.1.0
      </div>
    </div>
  );
};

export default LoadingScreen;
