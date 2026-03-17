package com.netzone.app

import java.net.InetAddress
import java.nio.ByteBuffer

data class ParsedPacket(
    val version: Int,
    val protocol: Int,
    val sourceAddress: String,
    val sourcePort: Int,
    val destinationAddress: String,
    val destinationPort: Int,
    val length: Int
)

object PacketParser {
    fun parse(data: ByteArray, length: Int): ParsedPacket? {
        if (length < 20) return null
        
        val buffer = ByteBuffer.wrap(data, 0, length)
        val versionAndIHL = buffer.get(0).toInt() and 0xFF
        val version = versionAndIHL shr 4
        
        return if (version == 4) {
            parseIPv4(buffer, length)
        } else if (version == 6) {
            parseIPv6(buffer, length)
        } else {
            null
        }
    }

    private fun parseIPv4(buffer: ByteBuffer, length: Int): ParsedPacket? {
        val ihl = (buffer.get(0).toInt() and 0x0F) * 4
        val protocol = buffer.get(9).toInt() and 0xFF
        
        val srcIp = ByteArray(4)
        buffer.position(12)
        buffer.get(srcIp)
        val sourceAddress = InetAddress.getByAddress(srcIp).hostAddress ?: "Unknown"
        
        val dstIp = ByteArray(4)
        buffer.position(16)
        buffer.get(dstIp)
        val destinationAddress = InetAddress.getByAddress(dstIp).hostAddress ?: "Unknown"
        
        var sourcePort = 0
        var destinationPort = 0
        
        if (protocol == 6 || protocol == 17) { // TCP or UDP
            buffer.position(ihl)
            sourcePort = buffer.getShort().toInt() and 0xFFFF
            destinationPort = buffer.getShort().toInt() and 0xFFFF
        }
        
        return ParsedPacket(4, protocol, sourceAddress, sourcePort, destinationAddress, destinationPort, length)
    }

    private fun parseIPv6(buffer: ByteBuffer, length: Int): ParsedPacket? {
        if (length < 40) return null
        val protocol = buffer.get(6).toInt() and 0xFF
        
        val srcIp = ByteArray(16)
        buffer.position(8)
        buffer.get(srcIp)
        val sourceAddress = InetAddress.getByAddress(srcIp).hostAddress ?: "Unknown"
        
        val dstIp = ByteArray(16)
        buffer.position(24)
        buffer.get(dstIp)
        val destinationAddress = InetAddress.getByAddress(dstIp).hostAddress ?: "Unknown"
        
        var sourcePort = 0
        var destinationPort = 0
        
        if (protocol == 6 || protocol == 17) { // TCP or UDP
            buffer.position(40)
            sourcePort = buffer.getShort().toInt() and 0xFFFF
            destinationPort = buffer.getShort().toInt() and 0xFFFF
        }
        
        return ParsedPacket(6, protocol, sourceAddress, sourcePort, destinationAddress, destinationPort, length)
    }
}
