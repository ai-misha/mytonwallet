//
//  CatArt.swift
//  WalletContext
//

import UIKit

/// Colors of every cat in the app: a ginger tabby that gets a bit darker at night.
public enum CatColors {
    public static let fur = UIColor(light: "#F59A4B", dark: "#E8893E")
    public static let furDark = UIColor(light: "#D9772A", dark: "#B9611F")
    public static let stripe = UIColor(light: "#E07F30", dark: "#C96F28")
    public static let skin = UIColor(light: "#FFB8C3", dark: "#E89AA6")
    public static let nose = UIColor(light: "#FF8FA3", dark: "#F07F94")
    public static let eye = UIColor(light: "#3A2A1E", dark: "#1A120C")
    /// Cats' eyes glow in the dark
    public static let iris = UIColor(light: "#F2B33D", dark: "#C9F25A")
    public static let muzzle = UIColor(red: 1, green: 243 / 255, blue: 230 / 255, alpha: 1)
    public static let whisker = UIColor(red: 70 / 255, green: 50 / 255, blue: 35 / 255, alpha: 0.55)
    public static let cardWhisker = UIColor { traits in
        traits.userInterfaceStyle == .dark
            ? UIColor(red: 1, green: 236 / 255, blue: 220 / 255, alpha: 0.45)
            : UIColor(red: 150 / 255, green: 105 / 255, blue: 70 / 255, alpha: 0.55)
    }
    public static let pawPrint = UIColor { traits in
        traits.userInterfaceStyle == .dark
            ? UIColor(red: 1, green: 162 / 255, blue: 92 / 255, alpha: 0.4)
            : UIColor(red: 242 / 255, green: 138 / 255, blue: 58 / 255, alpha: 0.45)
    }
    /// Semi-transparent pink, so the inner ear works on any button color
    public static let innerEarOnTint = UIColor(red: 1, green: 184 / 255, blue: 195 / 255, alpha: 0.75)
}

/// Shapes shared by all the cats: ears and paw prints, the same art as in the web version of the theme.
@MainActor
public enum CatShapes {

    /// A rounded triangle standing on the bottom edge of `rect`.
    public static func ear(in rect: CGRect) -> UIBezierPath {
        let path = UIBezierPath()
        path.move(to: point(in: rect, x: 0, y: 1))
        path.addLine(to: point(in: rect, x: 0.37, y: 0.122))
        path.addQuadCurve(to: point(in: rect, x: 0.63, y: 0.122), controlPoint: point(in: rect, x: 0.5, y: -0.1))
        path.addLine(to: point(in: rect, x: 1, y: 1))
        path.close()
        return path
    }

    /// The pink inside of an ear drawn with `ear(in:)`.
    public static func innerEar(in rect: CGRect) -> UIBezierPath {
        let path = UIBezierPath()
        path.move(to: point(in: rect, x: 0.25, y: 1))
        path.addLine(to: point(in: rect, x: 0.445, y: 0.41))
        path.addQuadCurve(to: point(in: rect, x: 0.555, y: 0.41), controlPoint: point(in: rect, x: 0.5, y: 0.3))
        path.addLine(to: point(in: rect, x: 0.75, y: 1))
        path.close()
        return path
    }

    /// A paw print (one main pad and four toe beans) fitted into a `size × size` square around `center`.
    public static func paw(center: CGPoint, size: CGFloat, rotation: CGFloat = 0) -> UIBezierPath {
        // x, y, radius x, radius y, rotation in degrees; in a 24 × 24 design box
        let pads: [[CGFloat]] = [
            [12, 16.2, 5.4, 4.5, 0],
            [4.9, 10.6, 2.2, 2.9, -22],
            [9.3, 5.9, 2.35, 3.05, -6],
            [14.7, 5.9, 2.35, 3.05, 6],
            [19.1, 10.6, 2.2, 2.9, 22],
        ]
        let path = UIBezierPath()
        for pad in pads {
            let oval = UIBezierPath(ovalIn: CGRect(x: -pad[2], y: -pad[3], width: pad[2] * 2, height: pad[3] * 2))
            oval.apply(CGAffineTransform(rotationAngle: pad[4] * .pi / 180))
            oval.apply(CGAffineTransform(translationX: pad[0] - 12, y: pad[1] - 12))
            path.append(oval)
        }
        let unit = size / 24
        path.apply(CGAffineTransform(scaleX: unit, y: unit))
        path.apply(CGAffineTransform(rotationAngle: rotation))
        path.apply(CGAffineTransform(translationX: center.x, y: center.y))
        return path
    }

    private static func point(in rect: CGRect, x: CGFloat, y: CGFloat) -> CGPoint {
        CGPoint(x: rect.minX + rect.width * x, y: rect.minY + rect.height * y)
    }
}

/// How a cat head looks right now.
public struct CatHeadPose: Equatable, Sendable {
    /// Pupil offset in design units (±3)
    public var look: CGPoint
    /// 0 = eyes open, 1 = closed
    public var blink: CGFloat
    public var isSleeping: Bool
    public var isHappy: Bool

    public init(look: CGPoint = .zero, blink: CGFloat = 0, isSleeping: Bool = false, isHappy: Bool = false) {
        self.look = look
        self.blink = blink
        self.isSleeping = isSleeping
        self.isHappy = isHappy
    }
}

/// Draws a ginger cat head, designed in a 100 × 90 box, into the current graphics context.
@MainActor
public enum CatHeadRenderer {
    /// Height divided by width
    public static let aspectRatio: CGFloat = 0.9

    public static func draw(in rect: CGRect, pose: CatHeadPose) {
        guard let context = UIGraphicsGetCurrentContext(), rect.width > 0, rect.height > 0 else { return }
        let scale = min(rect.width / 100, rect.height / 90)
        context.saveGState()
        context.translateBy(x: rect.midX - 50 * scale, y: rect.midY - 45 * scale)
        context.scaleBy(x: scale, y: scale)

        let leftEar = UIBezierPath()
        leftEar.move(to: CGPoint(x: 13, y: 44))
        leftEar.addLine(to: CGPoint(x: 19, y: 5))
        leftEar.addQuadCurve(to: CGPoint(x: 26, y: 3), controlPoint: CGPoint(x: 21, y: -1))
        leftEar.addLine(to: CGPoint(x: 47, y: 25))
        leftEar.close()
        fill(leftEar, CatColors.fur)
        fill(triangle(CGPoint(x: 20, y: 35), CGPoint(x: 23, y: 13), CGPoint(x: 38, y: 26)), CatColors.skin)

        let rightEar = UIBezierPath()
        rightEar.move(to: CGPoint(x: 87, y: 44))
        rightEar.addLine(to: CGPoint(x: 81, y: 5))
        rightEar.addQuadCurve(to: CGPoint(x: 74, y: 3), controlPoint: CGPoint(x: 79, y: -1))
        rightEar.addLine(to: CGPoint(x: 53, y: 25))
        rightEar.close()
        fill(rightEar, CatColors.fur)
        fill(triangle(CGPoint(x: 80, y: 35), CGPoint(x: 77, y: 13), CGPoint(x: 62, y: 26)), CatColors.skin)

        fill(UIBezierPath(ovalIn: CGRect(x: 10, y: 22, width: 80, height: 66)), CatColors.fur)
        fill(UIBezierPath(ovalIn: CGRect(x: 33, y: 58, width: 34, height: 24)), CatColors.muzzle.withAlphaComponent(0.9))

        let stripes: [[CGFloat]] = [
            [50, 24, 50, 34], [41, 25, 43.5, 33], [59, 25, 56.5, 33],
            [11, 52, 20, 54], [11, 60, 19, 60], [89, 52, 80, 54], [89, 60, 81, 60],
        ]
        stroke(lines(stripes), CatColors.stripe, width: 3.2)

        let blush = CatColors.nose.withAlphaComponent(0.35)
        fill(UIBezierPath(ovalIn: CGRect(x: 18, y: 63.5, width: 12, height: 7)), blush)
        fill(UIBezierPath(ovalIn: CGRect(x: 70, y: 63.5, width: 12, height: 7)), blush)

        if pose.isSleeping || pose.isHappy {
            let closedEyes = UIBezierPath()
            closedEyes.move(to: CGPoint(x: 27, y: 53))
            closedEyes.addQuadCurve(to: CGPoint(x: 41, y: 53), controlPoint: CGPoint(x: 34, y: 59))
            closedEyes.move(to: CGPoint(x: 59, y: 53))
            closedEyes.addQuadCurve(to: CGPoint(x: 73, y: 53), controlPoint: CGPoint(x: 66, y: 59))
            stroke(closedEyes, CatColors.eye, width: 2.6)
        } else {
            drawOpenEyes(context: context, pose: pose)
        }

        let nose = UIBezierPath()
        nose.move(to: CGPoint(x: 45.5, y: 63.5))
        nose.addQuadCurve(to: CGPoint(x: 54.5, y: 63.5), controlPoint: CGPoint(x: 50, y: 61.5))
        nose.addLine(to: CGPoint(x: 50, y: 68.5))
        nose.close()
        fill(nose, CatColors.nose)

        let mouth = UIBezierPath()
        mouth.move(to: CGPoint(x: 50, y: 68.5))
        mouth.addQuadCurve(to: CGPoint(x: 44, y: 73.5), controlPoint: CGPoint(x: 50, y: 74))
        mouth.move(to: CGPoint(x: 50, y: 68.5))
        mouth.addQuadCurve(to: CGPoint(x: 56, y: 73.5), controlPoint: CGPoint(x: 50, y: 74))
        stroke(mouth, CatColors.eye, width: 1.8)

        if pose.isHappy {
            let tongue = UIBezierPath()
            tongue.move(to: CGPoint(x: 47.5, y: 73))
            tongue.addQuadCurve(to: CGPoint(x: 52.5, y: 73), controlPoint: CGPoint(x: 50, y: 79))
            tongue.close()
            fill(tongue, CatColors.nose)
        }

        let whiskers: [[CGFloat]] = [
            [32, 67, 4, 61], [32, 70.5, 3, 72], [33, 74, 7, 82],
            [68, 67, 96, 61], [68, 70.5, 97, 72], [67, 74, 93, 82],
        ]
        stroke(lines(whiskers), CatColors.whisker, width: 1.2)

        context.restoreGState()
    }

    /// The cat head rendered into an image, resolving the colors for `traitCollection`.
    public static func image(size: CGSize, pose: CatHeadPose, traitCollection: UITraitCollection) -> UIImage {
        var image = UIImage()
        traitCollection.performAsCurrent {
            image = UIGraphicsImageRenderer(size: size).image { _ in
                draw(in: CGRect(origin: .zero, size: size), pose: pose)
            }
        }
        return image.withRenderingMode(.alwaysOriginal)
    }

    private static func drawOpenEyes(context: CGContext, pose: CatHeadPose) {
        context.saveGState()
        context.translateBy(x: 0, y: 52)
        context.scaleBy(x: 1, y: max(0.1, 1 - pose.blink * 0.9))
        context.translateBy(x: 0, y: -52)
        let eyeCenters: [CGFloat] = [34, 66]
        for eyeX in eyeCenters {
            fill(UIBezierPath(ovalIn: CGRect(x: eyeX - 7.5, y: 43.5, width: 15, height: 17)), CatColors.iris)
            let pupil = CGRect(x: eyeX - 2.6 + pose.look.x, y: 45.6 + pose.look.y, width: 5.2, height: 12.8)
            fill(UIBezierPath(ovalIn: pupil), CatColors.eye)
            let shine = CGRect(x: eyeX + 0.7 + pose.look.x, y: 46.7 + pose.look.y, width: 3.6, height: 3.6)
            fill(UIBezierPath(ovalIn: shine), .white)
        }
        context.restoreGState()
    }

    private static func triangle(_ a: CGPoint, _ b: CGPoint, _ c: CGPoint) -> UIBezierPath {
        let path = UIBezierPath()
        path.move(to: a)
        path.addLine(to: b)
        path.addLine(to: c)
        path.close()
        return path
    }

    /// Separate straight segments, each given as `[fromX, fromY, toX, toY]`.
    private static func lines(_ segments: [[CGFloat]]) -> UIBezierPath {
        let path = UIBezierPath()
        for segment in segments {
            path.move(to: CGPoint(x: segment[0], y: segment[1]))
            path.addLine(to: CGPoint(x: segment[2], y: segment[3]))
        }
        return path
    }

    private static func fill(_ path: UIBezierPath, _ color: UIColor) {
        color.setFill()
        path.fill()
    }

    private static func stroke(_ path: UIBezierPath, _ color: UIColor, width: CGFloat) {
        color.setStroke()
        path.lineWidth = width
        path.lineCapStyle = .round
        path.lineJoinStyle = .round
        path.stroke()
    }
}
