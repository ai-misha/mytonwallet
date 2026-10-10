// A tiny Web Audio "meow" synthesizer: a buzzy source pushed through two formant filters whose
// frequencies glide the way a cat's "mi-a-ow" does. No audio files needed.

let audioContext: AudioContext | undefined;

const MEOW_VOLUME = 0.18;
const PURR_VOLUME = 0.12;

function getAudioContext() {
  if (!audioContext) {
    const AudioContextClass = window.AudioContext
      || (window as unknown as { webkitAudioContext?: typeof AudioContext }).webkitAudioContext;
    if (!AudioContextClass) return undefined;

    audioContext = new AudioContextClass();
  }

  if (audioContext.state === 'suspended') {
    void audioContext.resume();
  }

  return audioContext;
}

function createFormant(context: AudioContext, input: AudioNode, output: AudioNode, gain: number) {
  const filter = context.createBiquadFilter();
  filter.type = 'bandpass';
  filter.Q.value = 6;

  const filterGain = context.createGain();
  filterGain.gain.value = gain;

  input.connect(filter);
  filter.connect(filterGain);
  filterGain.connect(output);

  return filter;
}

export function playMeow(pitch = 1) {
  const context = getAudioContext();
  if (!context) return;

  const now = context.currentTime;
  const duration = 0.55 + Math.random() * 0.25;
  const basePitch = (520 + Math.random() * 120) * pitch;

  const source = context.createOscillator();
  source.type = 'sawtooth';
  source.frequency.setValueAtTime(basePitch * 0.85, now);
  source.frequency.linearRampToValueAtTime(basePitch * 1.25, now + duration * 0.3);
  source.frequency.linearRampToValueAtTime(basePitch * 1.1, now + duration * 0.6);
  source.frequency.exponentialRampToValueAtTime(basePitch * 0.7, now + duration);

  // A bit of vibrato makes it sound alive
  const vibrato = context.createOscillator();
  vibrato.frequency.value = 7;
  const vibratoGain = context.createGain();
  vibratoGain.gain.value = basePitch * 0.02;
  vibrato.connect(vibratoGain);
  vibratoGain.connect(source.frequency);

  const envelope = context.createGain();
  envelope.gain.setValueAtTime(0.0001, now);
  envelope.gain.exponentialRampToValueAtTime(MEOW_VOLUME, now + 0.06);
  envelope.gain.setValueAtTime(MEOW_VOLUME, now + duration * 0.55);
  envelope.gain.exponentialRampToValueAtTime(0.0001, now + duration);
  envelope.connect(context.destination);

  // "i" → "a" → "u": the first formant opens up and then closes, the second one falls
  const firstFormant = createFormant(context, source, envelope, 1);
  firstFormant.frequency.setValueAtTime(400, now);
  firstFormant.frequency.linearRampToValueAtTime(950, now + duration * 0.4);
  firstFormant.frequency.linearRampToValueAtTime(450, now + duration);

  const secondFormant = createFormant(context, source, envelope, 0.6);
  secondFormant.frequency.setValueAtTime(2400, now);
  secondFormant.frequency.linearRampToValueAtTime(1500, now + duration * 0.5);
  secondFormant.frequency.linearRampToValueAtTime(900, now + duration);

  source.start(now);
  vibrato.start(now);
  source.stop(now + duration + 0.05);
  vibrato.stop(now + duration + 0.05);
}

export function playPurr(duration = 1.6) {
  const context = getAudioContext();
  if (!context) return;

  const now = context.currentTime;

  // Low rumble, amplitude-modulated at ~25 Hz like a real purr
  const rumble = context.createOscillator();
  rumble.type = 'triangle';
  rumble.frequency.value = 48;

  const lowpass = context.createBiquadFilter();
  lowpass.type = 'lowpass';
  lowpass.frequency.value = 320;

  const tremolo = context.createGain();
  tremolo.gain.value = 0;

  const tremoloLfo = context.createOscillator();
  tremoloLfo.frequency.value = 25;
  const tremoloDepth = context.createGain();
  tremoloDepth.gain.value = 0.5;
  tremoloLfo.connect(tremoloDepth);
  tremoloDepth.connect(tremolo.gain);

  const envelope = context.createGain();
  envelope.gain.setValueAtTime(0.0001, now);
  envelope.gain.exponentialRampToValueAtTime(PURR_VOLUME, now + 0.2);
  envelope.gain.setValueAtTime(PURR_VOLUME, now + duration - 0.3);
  envelope.gain.exponentialRampToValueAtTime(0.0001, now + duration);

  rumble.connect(lowpass);
  lowpass.connect(tremolo);
  tremolo.connect(envelope);
  envelope.connect(context.destination);

  rumble.start(now);
  tremoloLfo.start(now);
  rumble.stop(now + duration + 0.05);
  tremoloLfo.stop(now + duration + 0.05);
}
