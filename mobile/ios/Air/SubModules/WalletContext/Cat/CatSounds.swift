//
//  CatSounds.swift
//  WalletContext
//

import AVFoundation

/// Meows and purrs. They use the ambient audio session, so the silent switch mutes them.
@MainActor
public enum CatSounds {
    private static var players: [String: AVAudioPlayer] = [:]

    public static func meow(pitch: Float = 1) {
        play("cat_meow", rate: pitch)
    }

    public static func purr() {
        play("cat_purr", rate: 1)
    }

    private static func play(_ name: String, rate: Float) {
        guard let player = player(named: name) else { return }
        do {
            try AVAudioSession.sharedInstance().setCategory(.ambient, options: .mixWithOthers)
            try AVAudioSession.sharedInstance().setActive(true)
        } catch {
            return
        }
        player.currentTime = 0
        player.rate = min(max(rate, 0.5), 2)
        player.play()
    }

    private static func player(named name: String) -> AVAudioPlayer? {
        if let player = players[name] {
            return player
        }
        guard let url = AirBundle.url(forResource: name, withExtension: "wav"),
              let player = try? AVAudioPlayer(contentsOf: url) else {
            return nil
        }
        player.enableRate = true
        player.volume = 0.7
        player.prepareToPlay()
        players[name] = player
        return player
    }
}
