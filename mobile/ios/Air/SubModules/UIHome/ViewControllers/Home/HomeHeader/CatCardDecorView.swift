//
//  CatCardDecorView.swift
//  UIHome
//

import UIKit
import WalletContext

/// Turns the wallet card into a cat.
///
/// The `.back` part sits behind the card: two ears stick out of its top edge and whiskers out of its sides.
/// The `.front` part sits on top of the card: two paws hold it from below.
/// Every now and then an ear twitches and the paws knead.
///
/// Everything is drawn with sublayers outside of the view bounds, so the view never takes touches.
final class CatCardDecorView: UIView {

    enum Part {
        case back
        case front
    }

    private static let twitchDelay: ClosedRange<TimeInterval> = 6...11
    private static let kneadDelay: ClosedRange<TimeInterval> = 9...15

    private let part: Part

    private let leftEar = CALayer()
    private let rightEar = CALayer()
    private let earOuters = [CAShapeLayer(), CAShapeLayer()]
    private let earInners = [CAShapeLayer(), CAShapeLayer()]
    private let earTufts = [CAShapeLayer(), CAShapeLayer()]
    private let whiskers = [CAShapeLayer(), CAShapeLayer()]

    private let leftPaw = CALayer()
    private let rightPaw = CALayer()
    private let pawBodies = [CALayer(), CALayer()]
    private let pawOuters = [CAShapeLayer(), CAShapeLayer()]
    private let pawLines = [CAShapeLayer(), CAShapeLayer()]
    private let pawBeans = [CAShapeLayer(), CAShapeLayer()]

    private var idleWorkItem: DispatchWorkItem?

    init(part: Part) {
        self.part = part
        super.init(frame: .zero)
        isUserInteractionEnabled = false
        isAccessibilityElement = false
        accessibilityElementsHidden = true
        backgroundColor = .clear

        switch part {
        case .back:
            for (index, ear) in [leftEar, rightEar].enumerated() {
                ear.anchorPoint = CGPoint(x: 0.5, y: 1)
                earTufts[index].fillColor = nil
                earTufts[index].lineCap = .round
                ear.addSublayer(earOuters[index])
                ear.addSublayer(earInners[index])
                ear.addSublayer(earTufts[index])
                layer.addSublayer(ear)
            }
            for whisker in whiskers {
                whisker.fillColor = nil
                whisker.lineCap = .round
                layer.addSublayer(whisker)
            }
        case .front:
            for (index, paw) in [leftPaw, rightPaw].enumerated() {
                pawLines[index].fillColor = nil
                pawLines[index].lineCap = .round
                pawBodies[index].addSublayer(pawOuters[index])
                pawBodies[index].addSublayer(pawLines[index])
                pawBodies[index].addSublayer(pawBeans[index])
                paw.addSublayer(pawBodies[index])
                layer.addSublayer(paw)
            }
        }
        updateColors()
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        switch part {
        case .back:
            layoutEars()
            layoutWhiskers()
        case .front:
            layoutPaws()
        }
        CATransaction.commit()
    }

    override func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        if traitCollection.hasDifferentColorAppearance(comparedTo: previousTraitCollection) {
            updateColors()
        }
    }

    override func didMoveToWindow() {
        super.didMoveToWindow()
        if window != nil {
            scheduleIdleAnimation()
        } else {
            idleWorkItem?.cancel()
            idleWorkItem = nil
        }
    }

    // MARK: - Layout

    private func layoutEars() {
        let width = bounds.width
        guard width > 0 else { return }
        let earWidth = min(max(width * 0.17, 48), 80)
        let scale = earWidth / 56
        let earHeight = 46 * scale
        let earBounds = CGRect(x: 0, y: 0, width: earWidth, height: earHeight)
        let top = -earHeight * 0.68
        let transform = CGAffineTransform(scaleX: scale, y: scale)

        for (index, ear) in [leftEar, rightEar].enumerated() {
            let side: CGFloat = index == 0 ? -1 : 1
            let left = index == 0 ? width * 0.09 : width * 0.91 - earWidth
            ear.transform = CATransform3DIdentity
            ear.bounds = earBounds
            ear.position = CGPoint(x: left + earWidth / 2, y: top + earHeight)
            ear.transform = CATransform3DMakeRotation(side * 14 * .pi / 180, 0, 0, 1)
            for shape in [earOuters[index], earInners[index], earTufts[index]] {
                shape.frame = earBounds
            }
            earOuters[index].path = Self.scaled(Self.earOuterPath(), by: transform)
            earInners[index].path = Self.scaled(Self.earInnerPath(), by: transform)
            earTufts[index].path = Self.scaled(Self.earTuftsPath(), by: transform)
            earTufts[index].lineWidth = 1.4 * scale
        }
    }

    private func layoutWhiskers() {
        let whiskerWidth: CGFloat = 30
        let scale = whiskerWidth / 40
        let top = bounds.height * 0.52
        for (index, whisker) in whiskers.enumerated() {
            let transform: CGAffineTransform
            if index == 0 {
                transform = CGAffineTransform(translationX: -whiskerWidth * 0.55, y: top).scaledBy(x: scale, y: scale)
            } else {
                transform = CGAffineTransform(translationX: bounds.width + whiskerWidth * 0.55, y: top).scaledBy(x: -scale, y: scale)
            }
            whisker.frame = bounds
            whisker.path = Self.scaled(Self.whiskersPath(), by: transform)
            whisker.lineWidth = 1.3
        }
    }

    private func layoutPaws() {
        let width = bounds.width
        guard width > 0 else { return }
        let pawWidth = min(max(width * 0.12, 38), 56)
        let scale = pawWidth / 48
        let pawHeight = 30 * scale
        let pawBounds = CGRect(x: 0, y: 0, width: pawWidth, height: pawHeight)
        let transform = CGAffineTransform(scaleX: scale, y: scale)

        for (index, paw) in [leftPaw, rightPaw].enumerated() {
            let side: CGFloat = index == 0 ? -1 : 1
            let left = index == 0 ? width * 0.14 : width * 0.86 - pawWidth
            paw.frame = CGRect(x: left, y: bounds.height - pawHeight * 0.57, width: pawWidth, height: pawHeight)
            let body = pawBodies[index]
            body.transform = CATransform3DIdentity
            body.frame = pawBounds
            body.transform = CATransform3DMakeRotation(side * 6 * .pi / 180, 0, 0, 1)
            for shape in [pawOuters[index], pawLines[index], pawBeans[index]] {
                shape.frame = pawBounds
            }
            pawOuters[index].path = Self.scaled(Self.pawOuterPath(), by: transform)
            pawLines[index].path = Self.scaled(Self.pawLinesPath(), by: transform)
            pawLines[index].lineWidth = 1.4 * scale
            pawBeans[index].path = Self.scaled(Self.pawBeansPath(), by: transform)
        }
    }

    private func updateColors() {
        let fur = CatColors.fur.resolvedColor(with: traitCollection).cgColor
        let furDark = CatColors.furDark.resolvedColor(with: traitCollection).cgColor
        let skin = CatColors.skin.resolvedColor(with: traitCollection).cgColor
        let whisker = CatColors.cardWhisker.resolvedColor(with: traitCollection).cgColor
        let muzzle = CatColors.muzzle.cgColor
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        for index in 0..<2 {
            earOuters[index].fillColor = fur
            earInners[index].fillColor = skin
            earTufts[index].strokeColor = muzzle
            whiskers[index].strokeColor = whisker
            pawOuters[index].fillColor = fur
            pawLines[index].strokeColor = furDark
            pawBeans[index].fillColor = skin
        }
        CATransaction.commit()
    }

    // MARK: - Idle animations

    private func scheduleIdleAnimation() {
        idleWorkItem?.cancel()
        let delay = part == .back ? TimeInterval.random(in: Self.twitchDelay) : TimeInterval.random(in: Self.kneadDelay)
        let workItem = DispatchWorkItem { [weak self] in
            guard let self else { return }
            playIdleAnimation()
            scheduleIdleAnimation()
        }
        idleWorkItem = workItem
        DispatchQueue.main.asyncAfter(deadline: .now() + delay, execute: workItem)
    }

    private func playIdleAnimation() {
        guard window != nil, alpha > 0, !isHidden, !UIAccessibility.isReduceMotionEnabled else { return }
        switch part {
        case .back:
            let isLeft = Bool.random()
            let side: CGFloat = isLeft ? -1 : 1
            let base = side * 14 * .pi / 180
            let twitch = CAKeyframeAnimation(keyPath: "transform.rotation.z")
            twitch.values = [base, base + side * 0.35, base - side * 0.1, base + side * 0.17, base]
            twitch.keyTimes = [0, 0.2, 0.45, 0.7, 1]
            twitch.duration = 0.6
            (isLeft ? leftEar : rightEar).add(twitch, forKey: "twitch")
        case .front:
            for (index, paw) in [leftPaw, rightPaw].enumerated() {
                let knead = CAKeyframeAnimation(keyPath: "position.y")
                let y = paw.position.y
                knead.values = [y, y - 4, y, y - 3, y, y - 2, y]
                knead.duration = 1.4
                knead.beginTime = CACurrentMediaTime() + (index == 0 ? 0 : 0.2)
                paw.add(knead, forKey: "knead")
            }
        }
    }

    private static func scaled(_ path: UIBezierPath, by transform: CGAffineTransform) -> CGPath {
        path.apply(transform)
        return path.cgPath
    }

    // MARK: - Art, in the same design boxes as the web version (ear 56 × 46, paw 48 × 30, whiskers 40 × 30)

    private static func earOuterPath() -> UIBezierPath {
        let path = UIBezierPath()
        path.move(to: CGPoint(x: 2, y: 46))
        path.addLine(to: CGPoint(x: 20, y: 5))
        path.addQuadCurve(to: CGPoint(x: 32, y: 4), controlPoint: CGPoint(x: 26, y: -3))
        path.addLine(to: CGPoint(x: 54, y: 46))
        path.close()
        return path
    }

    private static func earInnerPath() -> UIBezierPath {
        let path = UIBezierPath()
        path.move(to: CGPoint(x: 13, y: 46))
        path.addLine(to: CGPoint(x: 24, y: 16))
        path.addQuadCurve(to: CGPoint(x: 29.5, y: 16), controlPoint: CGPoint(x: 26.5, y: 11))
        path.addLine(to: CGPoint(x: 43, y: 46))
        path.close()
        return path
    }

    private static func earTuftsPath() -> UIBezierPath {
        let path = UIBezierPath()
        path.move(to: CGPoint(x: 24, y: 22))
        path.addLine(to: CGPoint(x: 22, y: 14))
        path.move(to: CGPoint(x: 27, y: 20))
        path.addLine(to: CGPoint(x: 27, y: 11))
        path.move(to: CGPoint(x: 30, y: 22))
        path.addLine(to: CGPoint(x: 32, y: 14))
        return path
    }

    private static func whiskersPath() -> UIBezierPath {
        let path = UIBezierPath()
        path.move(to: CGPoint(x: 40, y: 9))
        path.addQuadCurve(to: CGPoint(x: 1, y: 1), controlPoint: CGPoint(x: 20, y: 4))
        path.move(to: CGPoint(x: 40, y: 15))
        path.addQuadCurve(to: CGPoint(x: 0, y: 15), controlPoint: CGPoint(x: 20, y: 14))
        path.move(to: CGPoint(x: 40, y: 21))
        path.addQuadCurve(to: CGPoint(x: 2, y: 29), controlPoint: CGPoint(x: 20, y: 25))
        return path
    }

    private static func pawOuterPath() -> UIBezierPath {
        let path = UIBezierPath()
        path.move(to: CGPoint(x: 2, y: 30))
        path.addQuadCurve(to: CGPoint(x: 24, y: 3), controlPoint: CGPoint(x: 0, y: 4))
        path.addQuadCurve(to: CGPoint(x: 46, y: 30), controlPoint: CGPoint(x: 48, y: 4))
        path.close()
        return path
    }

    private static func pawLinesPath() -> UIBezierPath {
        let path = UIBezierPath()
        path.move(to: CGPoint(x: 16, y: 6))
        path.addLine(to: CGPoint(x: 16, y: 14))
        path.move(to: CGPoint(x: 24, y: 4.5))
        path.addLine(to: CGPoint(x: 24, y: 13))
        path.move(to: CGPoint(x: 32, y: 6))
        path.addLine(to: CGPoint(x: 32, y: 14))
        return path
    }

    private static func pawBeansPath() -> UIBezierPath {
        let path = UIBezierPath(ovalIn: CGRect(x: 17, y: 17, width: 14, height: 10))
        path.append(UIBezierPath(ovalIn: CGRect(x: 9.5, y: 13.6, width: 6, height: 6.8)))
        path.append(UIBezierPath(ovalIn: CGRect(x: 32.5, y: 13.6, width: 6, height: 6.8)))
        return path
    }
}
