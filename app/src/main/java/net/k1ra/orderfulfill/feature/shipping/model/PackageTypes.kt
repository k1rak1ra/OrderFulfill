package net.k1ra.orderfulfill.feature.shipping.model

enum class PackageTypes {
    LETTER, ENVELOPE, THICK_ENVELOPE, PARCEL;

    override fun toString(): String {
        return super.toString().lowercase()
    }
}