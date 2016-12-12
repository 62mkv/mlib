using System.Security.Cryptography;

namespace RedPrairie.MOCA.Client.Crypto
{
    /// <summary> 
    /// An implementation of SymmetricAlgorithm that implements the
    /// RedPrairie "bit flip" protocol.
    /// </summary>
    public class RPBFEncryptionStrategy : SymmetricAlgorithm
    {
        #region SymmetricAlgorithm Method Overrides

        ///<summary>
        ///When overridden in a derived class, creates a symmetric encryptor object with the specified <see cref="P:System.Security.Cryptography.SymmetricAlgorithm.Key"></see> property and initialization vector (<see cref="P:System.Security.Cryptography.SymmetricAlgorithm.IV"></see>).
        ///</summary>
        ///
        ///<returns>
        ///A symmetric encryptor object.
        ///</returns>
        ///
        ///<param name="rgbIV">The initialization vector to use for the symmetric algorithm. </param>
        ///<param name="rgbKey">The secret key to use for the symmetric algorithm. </param>
        public override ICryptoTransform CreateEncryptor(byte[] rgbKey, byte[] rgbIV)
        {
            return new RPBFCryptoTransform();
        }

        ///<summary>
        ///When overridden in a derived class, creates a symmetric decryptor object with the specified <see cref="P:System.Security.Cryptography.SymmetricAlgorithm.Key"></see> property and initialization vector (<see cref="P:System.Security.Cryptography.SymmetricAlgorithm.IV"></see>).
        ///</summary>
        ///
        ///<returns>
        ///A symmetric decryptor object.
        ///</returns>
        ///
        ///<param name="rgbIV">The initialization vector to use for the symmetric algorithm. </param>
        ///<param name="rgbKey">The secret key to use for the symmetric algorithm. </param>
        public override ICryptoTransform CreateDecryptor(byte[] rgbKey, byte[] rgbIV)
        {
            return new RPBFCryptoTransform();
        }

        ///<summary>
        ///When overridden in a derived class, generates a random key (<see cref="P:System.Security.Cryptography.SymmetricAlgorithm.Key"></see>) to use for the algorithm.
        ///</summary>
        public override void GenerateKey()
        {
            KeyValue = new byte[0];
        }

        ///<summary>
        ///When overridden in a derived class, generates a random initialization vector (<see cref="P:System.Security.Cryptography.SymmetricAlgorithm.IV"></see>) to use for the algorithm.
        ///</summary>
        public override void GenerateIV()
        {
            IVValue = new byte[0];
        }

        #endregion

        #region ToString() Method Overrides

        /// <summary>
        /// Returns a <see cref="T:System.String"></see> that represents the current <see cref="T:System.Object"></see>.
        /// </summary>
        /// <returns>
        /// A <see cref="T:System.String"></see> that represents the current <see cref="T:System.Object"></see>.
        /// </returns>
        public override string ToString()
        {
            return "rpbf";
        }

        #endregion
    }
}