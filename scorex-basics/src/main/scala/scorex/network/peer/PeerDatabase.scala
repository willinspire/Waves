package scorex.network.peer

import java.net.InetSocketAddress

//todo: add optional nonce
case class PeerInfo(lastSeen: Long, self: Boolean = false)

trait PeerDatabase {
  def addOrUpdateKnownPeer(peer: InetSocketAddress, peerInfo: PeerInfo): Unit

  def knownPeers(forSelf: Boolean): Seq[InetSocketAddress]

  def blacklistPeer(peer: InetSocketAddress): Unit

  def blacklistedPeers(): Seq[InetSocketAddress]

  def isBlacklisted(address: InetSocketAddress): Boolean
}
