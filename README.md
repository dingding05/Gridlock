# Gridlock 🚗💨

**Gridlock** is an innovative, native Android application engineered to streamline and optimize campus parking management. Built with a focus on modern UI/UX design principles and robust architecture, the platform aims to eliminate traffic inefficiencies and provide a seamless parking solution for students and faculty alike.

---

## 🛠️ Tech Stack & Architecture

* **Frontend & Logic:** Kotlin / Android Studio
* **UI/UX Design & Assets:** Figma
* **Version Control:** Git / GitHub
* **Features:** Adaptive app icon compatibility and streamlined navigation flows.

---

## 🎨 Visual Identity & Branding

The application features a modern, tech-focused geometric visual language designed for maximum visibility on high-resolution displays and mobile home screens.

| Application Icon | 
|---|
|<svg width="300" height="300" viewBox="0 0 300 300" fill="none" xmlns="http://www.w3.org/2000/svg">
  <defs>
    <linearGradient id="bgGrad" x1="0" y1="0" x2="300" y2="300" gradientUnits="userSpaceOnUse">
      <stop offset="0%" stop-color="#0D1924"/>
      <stop offset="100%" stop-color="#060C12"/>
    </linearGradient>
    <linearGradient id="gGrad" x1="120" y1="47" x2="253" y2="253" gradientUnits="userSpaceOnUse">
      <stop offset="0%" stop-color="#5FF4FF"/>
      <stop offset="55%" stop-color="#00E5FF"/>
      <stop offset="100%" stop-color="#00B8D0"/>
    </linearGradient>
    <filter id="glow" x="-30%" y="-30%" width="160%" height="160%">
      <feGaussianBlur in="SourceGraphic" stdDeviation="9" result="blur"/>
      <feMerge>
        <feMergeNode in="blur"/>
        <feMergeNode in="SourceGraphic"/>
      </feMerge>
    </filter>
    <filter id="innerHighlight" x="-5%" y="-5%" width="110%" height="110%">
      <feGaussianBlur in="SourceGraphic" stdDeviation="2" result="blur"/>
      <feMerge>
        <feMergeNode in="blur"/>
        <feMergeNode in="SourceGraphic"/>
      </feMerge>
    </filter>
    <clipPath id="roundedClip">
      <rect width="300" height="300" rx="68"/>
    </clipPath>
  </defs>

  <!-- Background -->
  <rect width="300" height="300" rx="68" fill="url(#bgGrad)"/>

  <!-- Subtle grid overlay -->
  <g clip-path="url(#roundedClip)" opacity="0.035">
    <line x1="0" y1="60" x2="300" y2="60" stroke="#00E5FF" stroke-width="1"/>
    <line x1="0" y1="120" x2="300" y2="120" stroke="#00E5FF" stroke-width="1"/>
    <line x1="0" y1="180" x2="300" y2="180" stroke="#00E5FF" stroke-width="1"/>
    <line x1="0" y1="240" x2="300" y2="240" stroke="#00E5FF" stroke-width="1"/>
    <line x1="60" y1="0" x2="60" y2="300" stroke="#00E5FF" stroke-width="1"/>
    <line x1="120" y1="0" x2="120" y2="300" stroke="#00E5FF" stroke-width="1"/>
    <line x1="180" y1="0" x2="180" y2="300" stroke="#00E5FF" stroke-width="1"/>
    <line x1="240" y1="0" x2="240" y2="300" stroke="#00E5FF" stroke-width="1"/>
  </g>

  <!-- Subtle rim highlight -->
  <rect x="1" y="1" width="298" height="298" rx="67.5" stroke="#00E5FF" stroke-opacity="0.1" stroke-width="1.5" fill="none"/>

  <!-- G glow bloom -->
  <path filter="url(#glow)" fill="#00E5FF" fill-opacity="0.35" d="M 223,77 A 103,103,0,1,0,253,150 L 253,200 L 148,200 L 148,150 L 203,150 A 53,53,0,1,1,188,113 L 223,77 Z"/>

  <!-- G main shape -->
  <path fill="url(#gGrad)" d="M 223,77 A 103,103,0,1,0,253,150 L 253,200 L 148,200 L 148,150 L 203,150 A 53,53,0,1,1,188,113 L 223,77 Z"/>

  <!-- G top-edge highlight (thin bright stroke on top portion) -->
  <path
    fill="none"
    stroke="white"
    stroke-opacity="0.18"
    stroke-width="1.5"
    d="M 223,77 A 103,103,0,0,0,150,47"
  />
</svg> |<img width="300" height="300" alt="GridloclAppIcon" src="https://github.com/user-attachments/assets/2bad23ce-76e6-414c-b323-a4cbfab1e90c" />



> **Design Note:** The core logo is a custom-crafted geometric "G" built within a precise vector network system. The icon utilizes an adaptive, multi-layer asset configuration—separating the high-contrast cyan foreground from a deep matte-navy background layer to support native Android parallax and masking effects.

---

## 🚀 Key Features

* **Smart Campus Parking Logic:** Designed to efficiently handle vehicle spatial distribution and reduce bottlenecks.
* **Modern Adaptive UI:** Full optimization for diverse device aspect ratios and native launcher scaling.
* **Clean Vector Asset Implementation:** Zero-loss resolution scaling using custom SVG-to-XML pipeline paths.

---

## 📦 Local Setup & Execution
CURRENTLY FOR ANDROID ONLY
To clone the project repository and run the executable app locally:

1. Clone the repository:
```bash
   git clone [https://github.com/dingding05/Gridlock.git](https://github.com/dingding05/Gridlock.git)
