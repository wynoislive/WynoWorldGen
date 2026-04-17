import React, { useEffect, useRef } from 'react';
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

const App = () => {
  const heroContentRef = useRef();

  useEffect(() => {
    gsap.from(heroContentRef.current, {
      opacity: 0,
      y: 30,
      duration: 1.5,
      ease: 'power3.out'
    });
  }, []);

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text);
    // Add professional toast or feedback here
  };

  return (
    <div className="min-h-screen">
      {/* Hero Section */}
      <section className="relative h-screen flex items-center justify-center overflow-hidden">
        <Hero3D />
        <div ref={heroContentRef} className="relative z-10 text-center px-4 max-w-5xl">
          <div className="inline-flex items-center space-x-2 px-4 py-2 rounded-full glass border-cyan/30 text-cyan text-sm mb-8 animate-pulse">
            <span className="relative flex h-2 w-2">
              <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-cyan opacity-75"></span>
              <span className="relative inline-flex rounded-full h-2 w-2 bg-cyan"></span>
            </span>
            <span>Version 5.1.0 Released</span>
          </div>
          <h1 className="text-6xl md:text-8xl font-extrabold font-outfit tracking-tight mb-8">
            Expertly Managed <br />
            <span className="text-transparent bg-clip-text bg-gradient-to-r from-cyan via-blue-500 to-gold">
              Survival Worlds
            </span>
          </h1>
          <p className="text-xl text-gray-400 mb-12 max-w-2xl mx-auto leading-relaxed">
            High-performance world management with enterprise-grade data isolation. 
            Scale your SMP with cinematic precision and absolute reliability.
          </p>
          <div className="flex flex-col md:flex-row items-center justify-center gap-6">
            <a href="https://github.com/wynoislive/WynoWorldGen/releases" className="px-8 py-4 bg-cyan text-black font-bold rounded-full hover:cyan-glow transition-all flex items-center gap-2">
              <Download size={20} /> Download Latest
            </a>
            <a href="#setup" className="px-8 py-4 glass border-white/10 rounded-full hover:bg-white/5 transition-all flex items-center gap-2">
              Get Started <ChevronRight size={20} />
            </a>
          </div>
        </div>
        <div className="absolute bottom-10 left-1/2 -translate-x-1/2 text-gray-500 animate-bounce">
          Scroll to explore
        </div>
      </section>

      {/* Stats Section */}
      <section className="py-24 border-y border-white/5 bg-white/[0.02]">
        <div className="container mx-auto px-4 grid grid-cols-2 md:grid-cols-4 gap-12 text-center">
          {[
            { label: 'TPS Stability', value: '100%', icon: Zap },
            { label: 'Save Speed', value: '< 20ms', icon: Cpu },
            { label: 'Total Worlds', value: '∞', icon: Globe },
            { label: 'Database Ready', value: 'Dual', icon: Database },
          ].map((stat, i) => (
            <div key={i} className="space-y-2">
              <stat.icon className="mx-auto text-cyan/40 mb-4" size={32} />
              <div className="text-4xl font-extrabold font-outfit text-white">{stat.value}</div>
              <div className="text-gray-500 text-sm">{stat.label}</div>
            </div>
          ))}
        </div>
      </section>

      {/* Features Grid */}
      <section className="py-32 container mx-auto px-4">
        <div className="text-center mb-24">
          <h2 className="text-4xl md:text-5xl font-bold font-outfit mb-6">Built for Excellence</h2>
          <div className="h-1 w-24 bg-cyan mx-auto mb-12"></div>
        </div>
        <div className="grid md:grid-cols-3 gap-8">
          <FeatureCard 
            icon={Shield} 
            title="Data Isolation 2.0" 
            description="Prevents all forms of data-bleed. Inventories, XP, and advancement criteria are safely saved per-world."
            delay={0.1}
          />
          <FeatureCard 
            icon={Zap} 
            title="Auto-Updater" 
            description="Built-in maintenance system to keep your plugin up-to-date with 100% safe rollback capabilities."
            delay={0.2}
          />
          <FeatureCard 
            icon={Terminal} 
            title="Async Pipeline" 
            description="Asynchronous data pipeline ensuring background database operations never impact your server TPS."
            delay={0.3}
          />
        </div>
      </section>

      {/* Installation Section */}
      <section id="setup" className="py-32 bg-white/[0.01]">
        <div className="container mx-auto px-4 max-w-4xl">
          <div className="glass rounded-3xl overflow-hidden border-gold/20">
            <div className="p-12 md:p-20">
              <h2 className="text-4xl font-bold mb-8 font-outfit">Quick Deployment</h2>
              <p className="text-gray-400 mb-12 text-lg">
                Get up and running in minutes. WynoWorldGen is designed for rapid, configuration-first installation.
              </p>
              
              <div className="space-y-6">
                <div className="bg-black/40 p-6 rounded-xl border border-white/5 flex items-center justify-between group">
                  <code className="text-cyan font-mono text-lg">/fw create survival_1 HARD tight</code>
                  <button onClick={() => copyToClipboard('/fw create survival_1 HARD tight')} className="text-gray-500 hover:text-white transition-colors">
                    Copy
                  </button>
                </div>
                <div className="bg-black/40 p-6 rounded-xl border border-white/5 flex items-center justify-between group">
                  <code className="text-gold font-mono text-lg">/fw join survival_1</code>
                  <button onClick={() => copyToClipboard('/fw join survival_1')} className="text-gray-500 hover:text-white transition-colors">
                    Copy
                  </button>
                </div>
              </div>

              <div className="mt-16 flex items-center gap-8 border-t border-white/5 pt-12">
                <div className="flex -space-x-4">
                  {[1, 2, 3].map(i => (
                    <div key={i} className="w-12 h-12 rounded-full border-2 border-onyx bg-gray-800" />
                  ))}
                </div>
                <p className="text-sm text-gray-500">Trusted by over 500+ servers worldwide</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="py-20 border-t border-white/5 glass">
        <div className="container mx-auto px-4 flex flex-col md:flex-row items-center justify-between gap-8">
          <div className="flex items-center gap-4">
            <div className="w-10 h-10 bg-gradient-to-br from-gold to-cyan rounded-lg"></div>
            <span className="font-outfit font-bold text-xl tracking-tighter">WynoWorldGen</span>
          </div>
          <div className="flex items-center gap-8 text-gray-400">
            <a href="https://github.com/wynoislive/WynoWorldGen" className="hover:text-cyan transition-colors flex items-center gap-2">
              <GitBranch size={20} /> GitHub
            </a>
            <a href="https://github.com/wynoislive/WynoWorldGen/wiki" className="hover:text-cyan transition-colors">Documentation</a>
            <a href="https://discord.gg/9WJSP4Kqg4" className="hover:text-cyan transition-colors">Support</a>
          </div>
          <p className="text-sm text-gray-600">© 2026 WYNO. Professional Grade.</p>
        </div>
      </footer>
    </div>
  );
};

export default App;
