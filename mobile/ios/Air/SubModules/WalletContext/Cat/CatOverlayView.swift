//
//  CatOverlayView.swift
//  WalletContext
//

import UIKit

/// The cat layer on top of the whole window:
/// - every tap leaves a paw print that fades away;
/// - every now and then a ginger cat peeks out from the right edge of the screen and follows
///   your finger with its eyes. A tap makes it meow, three quick taps make it rain cats.
///
/// The view never takes touches except the ones that land on the peeking cat.
public final class CatOverlayView: UIView {

    private static let firstPeekDelay: ClosedRange<TimeInterval> = 8...15
    private static let nextPeekDelay: ClosedRange<TimeInterval> = 35...80
    private static let peekDuration: TimeInterval = 5
    private static let happyDuration: TimeInterval = 1.4
    private static let maxPawPrints = 10
    private static let tapsForRain = 3
    private static let tapSeriesTimeout: TimeInterval = 0.9
    private static let rainDropCount = 28
    private static let rainEmojis = ["🐱", "😺", "😸", "😻", "🐾", "🐈", "😽", "🐟", "🧶"]
    private static let heartEmojis = ["💕", "✨", "💗"]

    private let catContainer = UIView()
    private let headView = CatHeadView()
    private let leftPaw = CatPeekingPawView()
    private let rightPaw = CatPeekingPawView()

    // 0 = hidden behind the screen edge, 1 = peeking, more = happy and leaning out
    private var peekAmount: CGFloat = 0
    private var isPeeking = false
    private var happyUntil = Date.distantPast
    private var lastTapDate = Date.distantPast
    private var tapCount = 0
    private var activePawPrints = 0
    private var isLeftPaw = false
    private var peekWorkItem: DispatchWorkItem?
    private var hideWorkItem: DispatchWorkItem?

    private var headWidth: CGFloat { 76 }
    private var headHeight: CGFloat { headWidth * CatHeadRenderer.aspectRatio }

    public override init(frame: CGRect) {
        super.init(frame: frame)
        backgroundColor = .clear
        isAccessibilityElement = false
        accessibilityElementsHidden = true

        catContainer.isHidden = true
        catContainer.addSubview(headView)
        catContainer.addSubview(leftPaw)
        catContainer.addSubview(rightPaw)
        catContainer.addGestureRecognizer(UITapGestureRecognizer(target: self, action: #selector(onCatTapped)))
        addSubview(catContainer)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    public override func didMoveToWindow() {
        super.didMoveToWindow()
        if window != nil {
            schedulePeek(after: .random(in: Self.firstPeekDelay))
        } else {
            peekWorkItem?.cancel()
            hideWorkItem?.cancel()
        }
    }

    public override func layoutSubviews() {
        super.layoutSubviews()
        layoutCat()
    }

    public override func point(inside point: CGPoint, with event: UIEvent?) -> Bool {
        guard isPeeking, !catContainer.isHidden else { return false }
        return catContainer.frame.contains(point)
    }

    /// Called by the window for every touch event before it is delivered.
    public func handle(touches: Set<UITouch>) {
        for touch in touches {
            let location = touch.location(in: self)
            switch touch.phase {
            case .began:
                addPawPrint(at: location)
                look(at: location)
            case .moved:
                look(at: location)
            default:
                break
            }
        }
    }

    // MARK: - Paw prints

    private func addPawPrint(at point: CGPoint) {
        guard !UIAccessibility.isReduceMotionEnabled, activePawPrints < Self.maxPawPrints else { return }
        activePawPrints += 1
        isLeftPaw.toggle()

        let size: CGFloat = 22
        let pawLayer = CAShapeLayer()
        pawLayer.bounds = CGRect(x: 0, y: 0, width: size, height: size)
        pawLayer.position = CGPoint(x: point.x + (isLeftPaw ? -5 : 5), y: point.y)
        pawLayer.path = CatShapes.paw(
            center: CGPoint(x: size / 2, y: size / 2),
            size: size,
            rotation: .random(in: -0.44...0.44)
        ).cgPath
        pawLayer.fillColor = CatColors.pawPrint.resolvedColor(with: traitCollection).cgColor
        // The final state; the animation below plays on top of it
        pawLayer.opacity = 0
        layer.addSublayer(pawLayer)

        let fade = CAKeyframeAnimation(keyPath: "opacity")
        fade.values = [0, 1, 1, 0]
        fade.keyTimes = [0, 0.15, 0.3, 1]
        let stamp = CAKeyframeAnimation(keyPath: "transform.scale")
        stamp.values = [0.4, 1.1, 1, 1]
        stamp.keyTimes = [0, 0.15, 0.3, 1]
        let group = CAAnimationGroup()
        group.animations = [fade, stamp]
        group.duration = 0.9
        pawLayer.add(group, forKey: "stamp")

        DispatchQueue.main.asyncAfter(deadline: .now() + 0.95) { [weak self] in
            pawLayer.removeFromSuperlayer()
            self?.activePawPrints -= 1
        }
    }

    // MARK: - Peeking cat

    private func layoutCat() {
        catContainer.transform = .identity
        catContainer.bounds = CGRect(x: 0, y: 0, width: headWidth, height: headHeight)
        headView.frame = catContainer.bounds
        let pawWidth = headWidth * 0.27
        let pawHeight = pawWidth * 0.62
        let pawCenterY = headHeight * 0.66
        leftPaw.frame = CGRect(x: headWidth * 0.25 - pawWidth / 2, y: pawCenterY - pawHeight / 2, width: pawWidth, height: pawHeight)
        rightPaw.frame = CGRect(x: headWidth * 0.75 - pawWidth / 2, y: pawCenterY - pawHeight / 2, width: pawWidth, height: pawHeight)

        // Rotated a quarter turn counterclockwise, so the cat's chin points at the right edge of the screen
        catContainer.transform = CGAffineTransform(rotationAngle: -.pi / 2)
        let visible = headHeight * 0.66 * peekAmount
        catContainer.center = CGPoint(x: bounds.width + headHeight / 2 - visible, y: bounds.height * 0.34)
    }

    private func schedulePeek(after delay: TimeInterval) {
        peekWorkItem?.cancel()
        let workItem = DispatchWorkItem { [weak self] in
            self?.showCat()
        }
        peekWorkItem = workItem
        DispatchQueue.main.asyncAfter(deadline: .now() + delay, execute: workItem)
    }

    private func scheduleHide(after delay: TimeInterval) {
        hideWorkItem?.cancel()
        let workItem = DispatchWorkItem { [weak self] in
            self?.hideCat()
        }
        hideWorkItem = workItem
        DispatchQueue.main.asyncAfter(deadline: .now() + delay, execute: workItem)
    }

    private func showCat() {
        guard window != nil else { return }
        guard window?.windowScene?.activationState == .foregroundActive else {
            schedulePeek(after: .random(in: Self.nextPeekDelay))
            return
        }
        superview?.bringSubviewToFront(self)
        isPeeking = true
        catContainer.isHidden = false
        setPeekAmount(1)
        scheduleHide(after: Self.peekDuration)
    }

    private func hideCat() {
        if happyUntil > Date() {
            scheduleHide(after: Self.happyDuration)
            return
        }
        isPeeking = false
        setPeekAmount(0) { [weak self] in
            guard let self, !isPeeking else { return }
            catContainer.isHidden = true
        }
        schedulePeek(after: .random(in: Self.nextPeekDelay))
    }

    private func setPeekAmount(_ amount: CGFloat, completion: (() -> Void)? = nil) {
        peekAmount = amount
        guard !UIAccessibility.isReduceMotionEnabled else {
            layoutCat()
            completion?()
            return
        }
        UIView.animate(
            withDuration: amount > 0 ? 0.55 : 0.4,
            delay: 0,
            usingSpringWithDamping: amount > 0 ? 0.62 : 1,
            initialSpringVelocity: 0,
            options: [.allowUserInteraction, .beginFromCurrentState]
        ) {
            self.layoutCat()
        } completion: { _ in
            completion?()
        }
    }

    private func look(at point: CGPoint) {
        guard isPeeking else { return }
        let center = catContainer.center
        let dx = point.x - center.x
        let dy = point.y - center.y
        let distance = max(1, hypot(dx, dy))
        let strength = min(1, distance / 120) * 3
        // The cat is rotated: its X axis points up and its Y axis points right
        headView.pose.look = CGPoint(x: -dy / distance * strength, y: dx / distance * strength)
    }

    @objc private func onCatTapped() {
        let now = Date()
        tapCount = now.timeIntervalSince(lastTapDate) < Self.tapSeriesTimeout ? tapCount + 1 : 1
        lastTapDate = now

        if tapCount >= Self.tapsForRain {
            tapCount = 0
            startRain()
            CatSounds.purr()
        } else {
            CatSounds.meow(pitch: .random(in: 0.9...1.2))
        }

        happyUntil = now.addingTimeInterval(Self.happyDuration)
        headView.pose.isHappy = true
        setPeekAmount(1.27)
        showHearts()
        scheduleHide(after: Self.peekDuration)
        DispatchQueue.main.asyncAfter(deadline: .now() + Self.happyDuration) { [weak self] in
            guard let self, happyUntil <= Date() else { return }
            headView.pose.isHappy = false
            if isPeeking {
                setPeekAmount(1)
            }
        }
    }

    private func showHearts() {
        let origin = CGPoint(x: catContainer.frame.minX - 14, y: catContainer.center.y)
        for (index, emoji) in Self.heartEmojis.enumerated() {
            let label = UILabel()
            label.text = emoji
            label.font = .systemFont(ofSize: 18)
            label.sizeToFit()
            label.center = CGPoint(x: origin.x, y: origin.y + CGFloat(index - 1) * 20)
            label.alpha = 0
            addSubview(label)
            UIView.animateKeyframes(withDuration: 1.3, delay: Double(index) * 0.12, options: []) {
                UIView.addKeyframe(withRelativeStartTime: 0, relativeDuration: 0.25) {
                    label.alpha = 1
                }
                UIView.addKeyframe(withRelativeStartTime: 0, relativeDuration: 1) {
                    label.center.x -= 24
                }
                UIView.addKeyframe(withRelativeStartTime: 0.6, relativeDuration: 0.4) {
                    label.alpha = 0
                }
            } completion: { _ in
                label.removeFromSuperview()
            }
        }
    }

    private func startRain() {
        guard !UIAccessibility.isReduceMotionEnabled, bounds.width > 0 else { return }
        for _ in 0..<Self.rainDropCount {
            let label = UILabel()
            label.text = Self.rainEmojis.randomElement()
            label.font = .systemFont(ofSize: .random(in: 20...44))
            label.sizeToFit()
            label.center = CGPoint(x: .random(in: 0...bounds.width), y: -48)
            addSubview(label)
            let drift = CGFloat.random(in: -80...80)
            let spin = CGFloat.random(in: -3...3)
            let fallY = bounds.height + 48
            UIView.animate(
                withDuration: .random(in: 2.0...3.6),
                delay: .random(in: 0...0.9),
                options: [.curveEaseIn]
            ) {
                label.center = CGPoint(x: label.center.x + drift, y: fallY)
                label.transform = CGAffineTransform(rotationAngle: spin)
            } completion: { _ in
                label.removeFromSuperview()
            }
        }
    }
}

/// One of the two paws the peeking cat grips the screen edge with.
private final class CatPeekingPawView: UIView {

    override init(frame: CGRect) {
        super.init(frame: frame)
        isOpaque = false
        backgroundColor = .clear
        contentMode = .redraw
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func draw(_ rect: CGRect) {
        CatColors.fur.setFill()
        UIBezierPath(roundedRect: bounds, cornerRadius: bounds.height / 2).fill()
        CatColors.skin.setFill()
        CatShapes.paw(center: CGPoint(x: bounds.midX, y: bounds.midY), size: bounds.height * 0.9).fill()
    }

    override func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        if traitCollection.hasDifferentColorAppearance(comparedTo: previousTraitCollection) {
            setNeedsDisplay()
        }
    }
}
