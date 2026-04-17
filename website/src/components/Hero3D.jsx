import React, { useRef, useEffect, useMemo } from 'react';
import { Canvas, useFrame } from '@react-three/fiber';
import { OrbitControls, PerspectiveCamera, Float, MeshDistortMaterial, Sparkles, Stars, PresentationControls, Text, Environment } from '@react-three/drei';
import * as THREE from 'three';
import { gsap } from 'gsap';

const ParticleTrail = () => {
  const points = useRef([]);
  const meshRef = useRef();
  const count = 60;
  
  const [dummy] = React.useState(() => new THREE.Object3D());

  useFrame((state) => {
    const { x, y } = state.mouse;
    points.current.unshift({ x: x * 12, y: y * 12, z: (Math.sin(state.clock.elapsedTime * 2) * 2) });
    if (points.current.length > count) points.current.pop();

    points.current.forEach((p, i) => {
      dummy.position.set(p.x, p.y, p.z - i * 0.1);
      const s = (1 - i / count) * 0.5;
      dummy.scale.set(s, s, s);
      dummy.rotation.set(state.clock.elapsedTime + i, state.clock.elapsedTime * 0.5, 0);
      dummy.updateMatrix();
      meshRef.current.setMatrixAt(i, dummy.matrix);
    });
    meshRef.current.instanceMatrix.needsUpdate = true;
  });

  return (
    <instancedMesh ref={meshRef} args={[null, null, count]}>
      <boxGeometry args={[0.2, 0.2, 0.2]} />
      <meshStandardMaterial color="#00f7ff" emissive="#00f7ff" emissiveIntensity={4} transparent opacity={0.6} />
    </instancedMesh>
  );
};

const MagicBlock = ({ position, color, delay }) => {
  const mesh = useRef();
  
  useEffect(() => {
    // Magic Intro Animation: Float from above/random with rotation
    gsap.from(mesh.current.position, {
      y: position[1] + 10 + Math.random() * 5,
      x: position[0] + (Math.random() - 0.5) * 10,
      z: position[2] + (Math.random() - 0.5) * 10,
      duration: 2,
      delay: delay,
      ease: 'back.out(1.7)'
    });
    gsap.from(mesh.current.rotation, {
      x: Math.random() * Math.PI,
      y: Math.random() * Math.PI,
      duration: 2,
      delay: delay,
      ease: 'power4.out'
    });
  }, []);

  return (
    <mesh ref={mesh} position={position} castShadow receiveShadow>
      <boxGeometry args={[1, 1, 1]} />
      <meshStandardMaterial 
        color={color} 
        roughness={1} 
        metalness={0.1} // Vanilla style
        map={null} // To be replaced with textures if available
      />
    </mesh>
  );
};

const Island = () => {
  const groupRef = useRef();
  
  const blocks = useMemo(() => {
    const temp = [];
    for (let x = -3; x <= 3; x++) {
      for (let z = -3; z <= 3; z++) {
        const dist = Math.sqrt(x*x + z*z);
        if (dist > 3.5) continue;
        
        const h = Math.floor(Math.random() * 2);
        // Grass
        temp.push({ pos: [x, h, z], color: '#3f6b30', delay: dist * 0.1 });
        // Dirt
        temp.push({ pos: [x, h - 1, z], color: '#4b301a', delay: dist * 0.1 + 0.2 });
      }
    }
    return temp;
  }, []);

  useFrame((state) => {
    const { x, y } = state.mouse;
    groupRef.current.rotation.y += 0.002;
    // Master-class leaning logic
    groupRef.current.rotation.x = THREE.MathUtils.lerp(groupRef.current.rotation.x, -y * 0.15, 0.05);
    groupRef.current.rotation.z = THREE.MathUtils.lerp(groupRef.current.rotation.z, x * 0.15, 0.05);
  });

  return (
    <group ref={groupRef} scale={1}>
      {blocks.map((b, i) => (
        <MagicBlock key={i} position={b.pos} color={b.color} delay={b.delay} />
      ))}
      <Float speed={2} rotationIntensity={1} floatIntensity={1}>
        <Text
          position={[0, 4, 0]}
          fontSize={1.2}
          color="#ffffff"
          anchorX="center"
          anchorY="middle"
        >
          WYNO
          <meshStandardMaterial color="#00f7ff" emissive="#00f7ff" emissiveIntensity={2} />
        </Text>
      </Float>
    </group>
  );
};

const Hero3D = () => {
  return (
    <div className="absolute inset-0 z-0">
      <Canvas shadows camera={{ position: [15, 12, 15], fov: 35 }}>
        <fog attach="fog" args={['#050505', 10, 40]} />
        <color attach="background" args={['#050505']} />
        
        <ambientLight intensity={0.1} />
        <spotLight position={[20, 20, 20]} angle={0.2} penumbra={1} intensity={5} castShadow color="#00f7ff" />
        <pointLight position={[-10, 10, -10]} intensity={2} color="#ffcc00" />
        
        <ParticleTrail />

        <PresentationControls global config={{ mass: 2, tension: 500 }} snap={{ mass: 4, tension: 1500 }} rotation={[0, 0, 0]}>
          <Island />
        </PresentationControls>

        <Sparkles count={200} scale={20} size={2} speed={0.5} color="#00f7ff" opacity={1} />
        <Stars radius={100} depth={50} count={7000} factor={4} saturation={0} fade speed={1} />
        
        <Environment preset="night" />
      </Canvas>
    </div>
  );
};

export default Hero3D;
