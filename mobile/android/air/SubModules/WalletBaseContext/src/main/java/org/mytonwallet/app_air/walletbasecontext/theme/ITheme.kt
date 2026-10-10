package org.mytonwallet.app_air.walletbasecontext.theme

interface ITheme {
    fun getColor(color: WColor): Int
    fun getColor(color: WColor, isDark: Boolean): Int
}

private fun create(colormap: Map<WColor, Int>): IntArray = IntArray(WColor.entries.size).apply {
    colormap.forEach {
        set(it.key.ordinal, it.value)
    }
}

// 🐱 Ginger cat palette
const val DEFAULT_TINT_LIGHT = 0xFFF28A3A.toInt()
const val DEFAULT_TINT_DARK = 0xFFFF9A4D.toInt()

internal val THEME_LIGHT_PRESET
    get() = create(
        mapOf(
            WColor.Background to 0xFFFFFDF9.toInt(),
            WColor.PrimaryText to 0xFF2B2119.toInt(),
            WColor.PrimaryDarkText to 0xFF54483F.toInt(),
            WColor.PrimaryLightText to 0xFF8A7B70.toInt(),
            WColor.SecondaryText to 0xFF8A7B70.toInt(),
            WColor.SubtitleText to 0xFF928479.toInt(),
            WColor.Decimals to 0xFFA39689.toInt(),
            WColor.Tint to DEFAULT_TINT_LIGHT,
            WColor.TextOnTint to 0xFFFFFFFF.toInt(),
            WColor.SecondaryTextOnTint to 0xBFFFFFFF.toInt(),
            WColor.Separator to 0xFFF3E9DF.toInt(),
            WColor.SecondaryBackground to 0xFFFBF3EA.toInt(),
            WColor.TrinaryBackground to 0xFFFBF3EA.toInt(),
            WColor.GroupedBackground to 0xFFF8EDE1.toInt(),
            WColor.ThumbBackground to 0xFFF0E4D8.toInt(),
            WColor.BadgeBackground to 0xFFFBF1E7.toInt(),
            WColor.AttributesBackground to 0xFFFAF2EA.toInt(),
            WColor.PopupSeparator to 0x1F8A7B70,
            WColor.PopupWindow to 0x4C000000,
            WColor.PopupAmbientShadow to 0xCC000000.toInt(),
            WColor.PopupSpotShadow to 0x66000000,
            WColor.Thumb to 0xFFD6C8BC.toInt(),
            WColor.DIVIDER to 0xFF8E8E93.toInt(),
            WColor.Error to 0xFFFF3B30.toInt(),
            WColor.Green to 0xFF53A30D.toInt(),
            WColor.DarkGreen to 0xFF3CA930.toInt(),
            WColor.PositiveBalance to 0xFF7BFF9D.toInt(),
            WColor.Red to 0xFFFF3B30.toInt(),
            WColor.Buy to 0xFF49CA3B.toInt(),
            WColor.Sell to 0xFFFF5148.toInt(),
            WColor.Purple to 0xFF5E6BDE.toInt(),
            WColor.Orange to 0xFFF7931A.toInt(),
            WColor.StockBadge to 0xFFDE8C00.toInt(),
            WColor.EarnGradientLeft to 0xFF41C433.toInt(),
            WColor.EarnGradientRight to 0xFFF59A4B.toInt(),
            WColor.TintRipple to 0x20F28A3A,
            WColor.BackgroundRipple to 0x10000000,
            WColor.IncomingComment to 0xFF68D06E.toInt(),
            WColor.OutgoingComment to 0xFFF59A4B.toInt(),
            WColor.SearchFieldBackground to 0xFFFFFFFF.toInt(),
            WColor.Transparent to 0x00000000,
            WColor.White to 0xFFFFFFFF.toInt(),
            WColor.Black to 0xFF000000.toInt(),
            WColor.Icon to 0xFF3D3128.toInt()
        )
    )

internal val THEME_DARK_PRESET
    get() = create(
        mapOf(
            WColor.Background to 0xFF1E1916.toInt(),
            WColor.PrimaryText to 0xFFFBF5EF.toInt(),
            WColor.PrimaryDarkText to 0xFF7D7268.toInt(),
            WColor.PrimaryLightText to 0xFF9A8E84.toInt(),
            WColor.SecondaryText to 0xFF9A8E84.toInt(),
            WColor.SubtitleText to 0xFF9A8E84.toInt(),
            WColor.Decimals to 0xFF9A8E84.toInt(),
            WColor.Tint to DEFAULT_TINT_DARK,
            WColor.TextOnTint to 0xFFFFFFFF.toInt(),
            WColor.SecondaryTextOnTint to 0xBFFFFFFF.toInt(),
            WColor.Separator to 0xFF3A302A.toInt(),
            WColor.SecondaryBackground to 0xFF0E0B09.toInt(),
            WColor.TrinaryBackground to 0xFF2E2621.toInt(),
            WColor.GroupedBackground to 0xFF0A0807.toInt(),
            WColor.ThumbBackground to 0xFF26201C.toInt(),
            WColor.BadgeBackground to 0xFF2E2621.toInt(),
            WColor.AttributesBackground to 0xFF332A24.toInt(),
            WColor.PopupSeparator to 0x1FC2B4A8,
            WColor.PopupWindow to 0x4C000000,
            WColor.PopupAmbientShadow to 0xCC000000.toInt(),
            WColor.PopupSpotShadow to 0x66000000,
            WColor.Thumb to 0xFF5A4E46.toInt(),
            WColor.DIVIDER to 0xFF8E8E93.toInt(),
            WColor.Error to 0xFFFF3B30.toInt(),
            WColor.Green to 0xFF57C84A.toInt(),
            WColor.DarkGreen to 0xFF3CA930.toInt(),
            WColor.PositiveBalance to 0xFF7BFF9D.toInt(),
            WColor.Red to 0xFFFF5148.toInt(),
            WColor.Buy to 0xFF49CA3B.toInt(),
            WColor.Sell to 0xFFFF5148.toInt(),
            WColor.Purple to 0xFF7D88F4.toInt(),
            WColor.Orange to 0xFFF7931A.toInt(),
            WColor.StockBadge to 0xFFDE8C00.toInt(),
            WColor.EarnGradientLeft to 0xFF41C433.toInt(),
            WColor.EarnGradientRight to 0xFFFF9A4D.toInt(),
            WColor.TintRipple to 0x20FF9A4D,
            WColor.BackgroundRipple to 0x10FFFFFF,
            WColor.IncomingComment to 0xFF55A35A.toInt(),
            WColor.OutgoingComment to 0xFFD9772A.toInt(),
            WColor.SearchFieldBackground to 0xFF1E1916.toInt(),
            WColor.Transparent to 0x00000000,
            WColor.White to 0xFFFFFFFF.toInt(),
            WColor.Black to 0xFF000000.toInt(),
            WColor.Icon to 0xFFFFFFFF.toInt()
        )
    )
