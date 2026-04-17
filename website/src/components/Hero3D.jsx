import React, { useRef } from 'react';
import { Canvas, useFrame } from '@react-three/fiber';
import { OrbitControls, PerspectiveCamera, Float, MeshDistortMaterial } from '@react-three/drei';
import * as THREE from 'three';

const VoxelBlock = ({ position, color }) => {
  return (
    <mesh position={position}>
      <boxGeometry args={[1, 1, 1]} />
      <meshStandardMaterial color={color} />
    </mesh>
  );
};

const Island = () => {
  const groupRef = useRef();
  
  // Create a stylized 5x5x3 chunk
  const blocks = [];
  for (let x = -2; x <= 2; x++) {
    for (let z = -2; z <= 2; z++) {
      // Grass layer
      blocks.push({ pos: [x, 0, z], color: '#4ade80' });
      // Dirt layer
      blocks.push({ pos: [x, -1, z], color: '#713f12' });
      // Stone layer
      blocks.push({ pos: [x, -2, z], color: '#4b5563' });
    }
  }

  // Add random features (Trees, Crystals)
  blocks.push({ pos: [0, 1, 0], color: '#166534' }); // Tree trunk
  blocks.push({ pos: [0, 2, 0], color: '#22c55e' }); // Leaves
  blocks.push({ pos: [1, 1, -1], color: '#00f7ff' }); // Crystal

  useFrame((state) => {
    groupRef.current.rotation.y += 0.005;
  });

  return (
    <group ref={groupRef} scale={0.8}>
      {blocks.map((b, i) => (
        <VoxelBlock key={i} position={b.pos} color={b.color} />
      ))}
    </group>
  );
};

const Hero3D = () => {
  return (
    <div className="absolute inset-0 z-0 opacity-60">
      <Canvas shadows>
        <PerspectiveCamera makeDefault position={[10, 10, 10]} fov={35} />
        <ambientLight intensity={0.5} />
        <pointLight position={[10, 10, 10]} intensity={1.5} />
        <spotLight position={[-10, 10, 10]} angle={0.15} penumbra={1} intensity={1} />
        
        <Float speed={2} rotationIntensity={0.5} floatIntensity={0.5}>
          <Island />
        </Float>
        
        <OrbitControls enableZoom={false} enablePan={false} />
      </Canvas>
    </div>
  );
};

export default Hero3D;
