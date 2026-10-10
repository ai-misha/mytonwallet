//
//  CatHeadView.swift
//  WalletContext
//

import UIKit

/// A ginger cat head that blinks by itself every few seconds.
public final class CatHeadView: UIView {

    public var pose = CatHeadPose() {
        didSet {
            if pose != oldValue {
                setNeedsDisplay()
            }
        }
    }

    private var blinkTimer: Timer?

    public override init(frame: CGRect) {
        super.init(frame: frame)
        isOpaque = false
        backgroundColor = .clear
        contentMode = .redraw
        isAccessibilityElement = false
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    public override func draw(_ rect: CGRect) {
        CatHeadRenderer.draw(in: bounds, pose: pose)
    }

    public override func didMoveToWindow() {
        super.didMoveToWindow()
        blinkTimer?.invalidate()
        blinkTimer = nil
        guard window != nil, !UIAccessibility.isReduceMotionEnabled else { return }
        blinkTimer = Timer.scheduledTimer(withTimeInterval: 4.5, repeats: true) { [weak self] _ in
            MainActor.assumeIsolated {
                self?.blink()
            }
        }
    }

    public override func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        if traitCollection.hasDifferentColorAppearance(comparedTo: previousTraitCollection) {
            setNeedsDisplay()
        }
    }

    private func blink() {
        guard !pose.isSleeping, !pose.isHappy else { return }
        pose.blink = 1
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.16) { [weak self] in
            self?.pose.blink = 0
        }
    }
}
