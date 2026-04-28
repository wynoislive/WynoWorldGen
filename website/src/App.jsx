import React, { useEffect, useRef, useState, useMemo } from 'react';
import { gsap } from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';
import { Terminal, Shield, Cpu, Zap, Download, GitBranch, ChevronRight, Globe, Database, ArrowRight } from 'lucide-react';
import Hero3D from './components/Hero3D';

gsap.registerPlugin(ScrollTrigger);

const FeatureCard = ({ icon: Icon, title, description, delay }) => {
  const cardRef = useRef();

  useEffect(() => {
    gsap.from(cardRef.current, {
      scrollTrigger: {
        trigger: cardRef.current,
        start: 'top 85%',
      },
      opacity: 0,
      y: 50,
      duration: 1,
      delay: delay
    });
  }, [delay]);

  return (
    <div ref={cardRef} className="glass p-8 rounded-2xl hover:border-cyan/50 transition-all duration-300 group">
      <div className="w-14 h-14 bg-cyan/10 rounded-xl flex items-center justify-center mb-6 border border-cyan/20 group-hover:cyan-glow transition-all">
        <Icon className="text-cyan w-8 h-8" />
      </div>
      <h3 className="text-2xl font-bold mb-4 font-outfit">{title}</h3>
      <p className="text-gray-400 leading-relaxed">{description}</p>
    </div>
  );
};

import LoadingScreen from './components/LoadingScreen';

const App = () => {
  const [isStarted, setIsStarted] = useState(false);
  const heroContentRef = useRef();
  const [muted, setMuted] = useState(true);
  const audioCtx = useRef(null);
  const filter = useRef(null);

  const toggleAudio = (force = null) => {
    if (!audioCtx.current) {
      audioCtx.current = new (window.AudioContext || window.webkitAudioContext)();
      const noise = audioCtx.current.createBufferSource();
      const bufferSize = 2 * audioCtx.current.sampleRate;
      const buffer = audioCtx.current.createBuffer(1, bufferSize, audioCtx.current.sampleRate);
      const output = buffer.getChannelData(0);
      for (let i = 0; i < bufferSize; i++) { output[i] = Math.random() * 2 - 1; }
      
      noise.buffer = buffer;
      noise.loop = true;
      
      filter.current = audioCtx.current.createBiquadFilter();
      filter.current.type = 'lowpass';
      filter.current.frequency.value = 400;
      filter.current.Q.value = 10;

      const gain = audioCtx.current.createGain();
      gain.gain.value = 0.05;

      noise.connect(filter.current);
      filter.current.connect(gain);
      gain.connect(audioCtx.current.destination);
      noise.start();
    }
    
    const shouldMute = force !== null ? !force : !muted;
    if (!shouldMute) {
      audioCtx.current.resume();
    } else {
      audioCtx.current.suspend();
    }
    setMuted(shouldMute);
  };

  const onMissionStart = () => {
    setIsStarted(true);
    toggleAudio(true); // Un-mute on start
    
    // Cinematic Reveal Timeline
    const tl = gsap.timeline();
    tl.fromTo('.hero-hud', { opacity: 0, scale: 0.9 }, { 
      opacity: 1, 
      scale: 1, 
      duration: 2, 
      ease: 'power4.out',
      delay: 0.5 
    });
  };

  useEffect(() => {
    // Scroll Master-Class Pathing
    gsap.to('.hero-3d', {
      scrollTrigger: {
        trigger: 'body',
        start: 'top top',
        end: 'bottom bottom',
        scrub: 2,
      },
      scale: 2.2,
      rotateY: 180,
      y: 800,
      x: 300,
      opacity: 0.1,
      ease: 'none'
    });

    gsap.to('.hero-hud', {
      scrollTrigger: {
        trigger: 'body',
        start: 'top top',
        end: '30% top',
        scrub: 1,
      },
      y: -200,
      opacity: 0,
      filter: 'blur(20px)'
    });
  }, []);

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text);
  };

  return (
    <div className="min-h-screen w-full bg-[#050505] text-white flex flex-col overflow-x-hidden">
      {!isStarted && <LoadingScreen onStarted={onMissionStart} />}

      <button 
        onClick={() => toggleAudio()}
        className="fixed top-8 right-8 z-50 p-4 glass rounded-full hover:cyan-glow transition-all"
      >
        {muted ? <Globe className="text-gray-500" /> : <Zap className="text-cyan animate-pulse" />}
      </button>

      {/* Hero Section */}
      <section className="relative w-full h-screen min-h-[800px] flex items-center justify-center overflow-hidden">
        <div className="hero-3d absolute inset-0">
          <Hero3D />
        </div>
        
        <div className="hero-hud relative z-10 text-center px-4 w-full flex flex-col items-center justify-center">
          <div className="inline-flex items-center space-x-2 px-4 py-2 rounded-full glass border-cyan/30 text-cyan text-sm mb-8 animate-pulse">
            <span className="relative flex h-2 w-2">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-cyan opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2 w-2 bg-cyan"></span>
            </span>
            <span>Version 6.2.1 Released</span>
          </div>
          
          <h1 className="text-6xl md:text-9xl font-extrabold font-outfit tracking-tighter mb-8 leading-none">
            WYNO <br />
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-cyan via-blue-400 to-gold">
              WORLDGEN
            </span>
          </h1>
          
          <p className="text-xl text-gray-400 mb-12 max-w-2xl mx-auto leading-relaxed font-light">
            Cinematic World Management for Elite Minecraft SMPs. 
            Automated, Asynchronous, and Absolute.
          </p>
          
          <div className="flex flex-col md:flex-row items-center justify-center gap-6">
            <a href="https://github.com/wynoislive/WynoWorldGen/releases" className="px-10 py-5 bg-cyan text-black font-bold rounded-lg hover:cyan-glow hover:scale-105 transition-all flex items-center gap-2">
              <Download size={20} /> DEPLOY LATEST
            </a>
            <a href="#setup" className="px-10 py-5 glass border-white/10 rounded-lg hover:bg-white/5 transition-all flex items-center gap-2">
              SETUP GUIDE <ArrowRight size={20} />
            </a>
          </div>
        </div>
        
        <div className="absolute bottom-10 left-1/2 -translate-x-1/2 text-gray-500 animate-bounce flex flex-col items-center gap-2">
          <span className="text-[10px] tracking-[0.3em] uppercase opacity-50">Initiate Orbital Scroll</span>
          <ChevronRight className="rotate-90" />
        </div>
      </section>

      {/* Stats Section with HUD Reveal */}
      <section className="relative py-48 border-y border-white/5 bg-white/[0.02]">
        <div className="container mx-auto px-4 grid grid-cols-1 md:grid-cols-4 gap-20 text-center">
          {[
            { label: 'TPS STABILITY', value: '100%', icon: Zap },
            { label: 'SAVE SPEED', value: '< 20MS', icon: Cpu },
            { label: 'SUPPORTED WORLDS', value: 'UNLIMITED', icon: Globe },
            { label: 'DATA SECURITY', value: 'ENTERPRISE', icon: Shield },
          ].map((stat, i) => (
            <div key={i} className="group cursor-default">
              <div className="mb-6 flex justify-center opacity-40 group-hover:opacity-100 transition-opacity">
                <stat.icon size={40} className="text-cyan" />
              </div>
              <div className="text-5xl font-black font-outfit text-white mb-2 tracking-tight group-hover:text-cyan transition-colors">{stat.value}</div>
              <div className="text-gray-500 text-xs tracking-[0.2em] uppercase font-bold">{stat.label}</div>
            </div>
          ))}
        </div>
      </section>

      {/* Features Grid */}
      <section className="py-48 container mx-auto px-4">
        <div className="text-center mb-32">
          <h2 className="text-5xl md:text-7xl font-bold font-outfit mb-8 tracking-tighter italic">CORE ARCHITECTURE</h2>
          <div className="h-1 w-32 bg-cyan mx-auto"></div>
        </div>
        <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-12">
          <FeatureCard 
            icon={Shield} 
            title="DATA ISOLATION" 
            description="Robust inventory and player data isolation system. 100% collision proof across all dimensions."
            delay={0.1}
          />
          <FeatureCard 
            icon={Database} 
            title="COMPANION WORLDS" 
            description="Auto-generates private Nether and End dimensions for every featured world. True isolation."
            delay={0.2}
          />
          <FeatureCard 
            icon={Zap} 
            title="SMART UPDATER" 
            description="Automatic version checks with a safe rollback mechanism. Your server stays updated while you sleep."
            delay={0.3}
          />
          <FeatureCard 
            icon={GitBranch} 
            title="NATIVE PERSISTENCE" 
            description="Last-location tracking and shared dimension profiles. Join back exactly where you left off."
            delay={0.4}
          />
        </div>
      </section>

      {/* Installation HUD */}
      <section id="setup" className="py-48 bg-white/[0.01]">
        <div className="container mx-auto px-4 max-w-5xl">
          <div className="glass rounded-3xl overflow-hidden border-cyan/10">
            <div className="p-12 md:p-24 relative overflow-hidden">
              <div className="absolute top-0 right-0 w-64 h-64 bg-cyan/5 rounded-full blur-[100px]" />
              <h2 className="text-5xl font-bold mb-12 font-outfit italic tracking-tighter">INSTANT DEPLOYMENT</h2>
              
              <div className="space-y-8">
                <div className="bg-black/60 p-8 rounded-xl border border-white/5 flex items-center justify-between group hover:border-cyan/30 transition-all">
                  <div className="flex items-center gap-6">
                    <span className="text-white/20 font-mono">01</span>
                    <code className="text-cyan font-mono text-xl">/fw create survival_1 HARD tight</code>
                  </div>
                  <button onClick={() => copyToClipboard('/fw create survival_1 HARD tight')} className="text-gray-500 hover:text-white uppercase text-xs font-bold tracking-widest px-4 py-2 glass rounded">
                    Copy
                  </button>
                </div>
                <div className="bg-black/60 p-8 rounded-xl border border-white/5 flex items-center justify-between group hover:border-gold/30 transition-all">
                  <div className="flex items-center gap-6">
                    <span className="text-white/20 font-mono">02</span>
                    <code className="text-gold font-mono text-xl">/fw join survival_1</code>
                  </div>
                  <button onClick={() => copyToClipboard('/fw join survival_1')} className="text-gray-500 hover:text-white uppercase text-xs font-bold tracking-widest px-4 py-2 glass rounded">
                    Copy
                  </button>
                </div>
              </div>

              <div className="mt-20 flex flex-col md:flex-row items-center gap-12 border-t border-white/5 pt-16">
                <div className="text-sm font-mono text-white/40">
                  ESTABLISHED SINCE 2024 // TRUSTED BY 500+ SERVERS
                </div>
                <div className="flex -space-x-3">
                  {[1, 2, 3, 4, 5].map(i => (
                    <div key={i} className="w-10 h-10 rounded-lg border border-white/10 bg-gray-900 flex items-center justify-center text-[10px] font-bold">W</div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="py-32 border-t border-white/5 bg-[#050505] relative z-10">
        <div className="container mx-auto px-4 flex flex-col items-center">
          <div className="flex items-center gap-6 mb-16">
            <div className="w-12 h-12 bg-white rounded-lg flex items-center justify-center">
              <img src="logo.png" className="w-8" />
            </div>
            <span className="font-outfit font-black text-4xl tracking-tighter uppercase italic">WynoWorldGen</span>
          </div>
          
          <div className="flex flex-wrap justify-center gap-12 text-sm font-bold tracking-widest uppercase mb-20 text-gray-500">
            <a href="https://github.com/wynoislive/WynoWorldGen" className="hover:text-cyan transition-colors">GitHub Repository</a>
            <a href="https://github.com/wynoislive/WynoWorldGen/wiki" className="hover:text-cyan transition-colors">Documentation</a>
            <a href="https://discord.gg/9WJSP4Kqg4" className="hover:text-cyan transition-colors">Community Discord</a>
            <a href="#" className="hover:text-cyan transition-colors">Privacy Policy</a>
          </div>
          
          <div className="text-[10px] text-white/10 tracking-[1em] uppercase">
            Designed for the Multiverse // © 2026 WYNO
          </div>
        </div>
      </footer>
    </div>
  );
};

export default App;
