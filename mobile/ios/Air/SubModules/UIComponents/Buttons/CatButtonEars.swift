//
//  CatButtonEars.swift
//  UIComponents
//

import UIKit
import WalletContext

/// Cat ears sticking out of the top edge of a button.
///
/// The ears take the button's background color, so they always match it, and a button without
/// a background stays earless. They go flat for a moment while the button is pressed,
/// like a startled kitty, and spring back on release.
@MainActor
final class CatButtonEars {
    private static let uprightTilt: CGFloat = 14
    private static let flatTilt: CGFloat = 40
    private static let visiblePart: CGFloat = 0.62
    private static let minButtonWidth: CGFloat = 72

    private let container = CALayer()
    private let containerMask = CALayer()
    private let leftEar = CALayer()
    private let rightEar = CALayer()
    private let leftOuter = CAShapeLayer()
    private let leftInner = CAShapeLayer()
    private let rightOuter = CAShapeLayer()
    private let rightInner = CAShapeLayer()
    private var isFlattened = false

    init() {
        // Only the part above the button's top edge is visible
        containerMask.backgroundColor = UIColor.black.cgColor
        container.mask = containerMask
        for (ear, outer, inner) in [(leftEar, leftOuter, leftInner), (rightEar, rightOuter, rightInner)] {
            ear.anchorPoint = CGPoint(x: 0.5, y: 1)
            inner.fillColor = CatColors.innerEarOnTint.cgColor
            ear.addSublayer(outer)
            ear.addSublayer(inner)
            container.addSublayer(ear)
        }
    }

    func install(on view: UIView) {
        view.layer.insertSublayer(container, at: 0)
    }

    func layout(in bounds: CGRect, color: UIColor?, traitCollection: UITraitCollection) {
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        defer { CATransaction.commit() }

        let resolvedColor = color?.resolvedColor(with: traitCollection).cgColor
        guard let resolvedColor, resolvedColor.alpha > 0, bounds.width >= Self.minButtonWidth, bounds.height > 0 else {
            container.isHidden = true
            return
        }
        container.isHidden = false
        container.frame = bounds
        containerMask.frame = CGRect(x: -100, y: -100, width: bounds.width + 200, height: 100)

        let scale = min(max(bounds.height / 50, 0.7), 1.2)
        let earSize = CGSize(width: 21 * scale, height: 18 * scale)
        let earBounds = CGRect(origin: .zero, size: earSize)
        let earPath = CatShapes.ear(in: earBounds).cgPath
        let innerPath = CatShapes.innerEar(in: earBounds).cgPath
        for (ear, outer, inner) in [(leftEar, leftOuter, leftInner), (rightEar, rightOuter, rightInner)] {
            ear.bounds = earBounds
            outer.frame = earBounds
            inner.frame = earBounds
            outer.path = earPath
            inner.path = innerPath
            outer.fillColor = resolvedColor
        }

        // Keep the ears on the straight part of the top edge, away from the rounded corners
        let inset = max(bounds.height * 0.42, bounds.width * 0.08)
        let baseY = earSize.height * (1 - Self.visiblePart)
        leftEar.position = CGPoint(x: inset + earSize.width / 2, y: baseY)
        rightEar.position = CGPoint(x: bounds.width - inset - earSize.width / 2, y: baseY)
        leftEar.transform = earTransform(side: -1)
        rightEar.transform = earTransform(side: 1)
    }

    func setFlattened(_ flattened: Bool) {
        guard flattened != isFlattened else { return }
        isFlattened = flattened

        CATransaction.begin()
        if UIAccessibility.isReduceMotionEnabled {
            CATransaction.setDisableActions(true)
        } else if flattened {
            CATransaction.setAnimationDuration(0.12)
            CATransaction.setAnimationTimingFunction(CAMediaTimingFunction(name: .easeOut))
        } else {
            CATransaction.setAnimationDuration(0.35)
            CATransaction.setAnimationTimingFunction(CAMediaTimingFunction(controlPoints: 0.34, 1.56, 0.64, 1))
        }
        leftEar.transform = earTransform(side: -1)
        rightEar.transform = earTransform(side: 1)
        CATransaction.commit()
    }

    private func earTransform(side: CGFloat) -> CATransform3D {
        let tilt = isFlattened ? Self.flatTilt : Self.uprightTilt
        let rotation = CATransform3DMakeRotation(side * tilt * .pi / 180, 0, 0, 1)
        return isFlattened ? CATransform3DScale(rotation, 1, 0.7, 1) : rotation
    }
}
