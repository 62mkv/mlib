using System.Security.Cryptography;

namespace RedPrairie.MOCA.Client.Crypto
{
    /// <summary>
    /// An implementation of ICryptoTransform that implements the
    /// RedPrairie "bit flip" protocol.
    /// </summary>
    public class RPBFCryptoTransform : ICryptoTransform
    {
        #region Private Fields

        private int _count = 0;

        #endregion

        #region ICryptoTransform Members

        /// <summary>
        /// Gets a value indicating whether the current transform can be reused.
        /// </summary>
        /// <value></value>
        /// <returns>
        /// true if the current transform can be reused; otherwise, false.
        /// </returns>
        public bool CanReuseTransform
        {
            get { return false; }
        }

        /// <summary>
        /// Gets a value indicating whether multiple blocks can be transformed.
        /// </summary>
        /// <value></value>
        /// <returns>
        /// true if multiple blocks can be transformed; otherwise, false.
        /// </returns>
        public bool CanTransformMultipleBlocks
        {
            get { return true; }
        }

        /// <summary>
        /// Gets the input block size.
        /// </summary>
        /// <value></value>
        /// <returns>
        /// The size of the input data blocks in bytes.
        /// </returns>
        public int InputBlockSize
        {
            get { return 1; }
        }

        /// <summary>
        /// Gets the output block size.
        /// </summary>
        /// <value></value>
        /// <returns>
        /// The size of the output data blocks in bytes.
        /// </returns>
        public int OutputBlockSize
        {
            get { return 1; }
        }

        /// <summary>
        /// Transforms the specified region of the input byte array and copies the resulting transform to the specified region of the output byte array.
        /// </summary>
        /// <param name="inputBuffer">The input for which to compute the transform.</param>
        /// <param name="inputOffset">The offset into the input byte array from which to begin using data.</param>
        /// <param name="inputCount">The number of bytes in the input byte array to use as data.</param>
        /// <param name="outputBuffer">The output to which to write the transform.</param>
        /// <param name="outputOffset">The offset into the output byte array from which to begin writing data.</param>
        /// <returns>The number of bytes written.</returns>
        public int TransformBlock(byte[] inputBuffer, int inputOffset, int inputCount, byte[] outputBuffer,
                                           int outputOffset)
        {
            for (int i = 0; i < inputCount; i++)
            {
                byte mask = (byte)((_count++ % 255) + 1);
                byte input = (inputBuffer[i + inputOffset]);
                outputBuffer[i + outputOffset] = ((mask & input) != 0) ? (byte)(input ^ mask) : (byte)(input | mask);
            }
            return inputCount;
        }

        /// <summary>
        /// Transforms the specified region of the specified byte array.
        /// </summary>
        /// <param name="inputBuffer">The input for which to compute the transform.</param>
        /// <param name="inputOffset">The offset into the byte array from which to begin using data.</param>
        /// <param name="inputCount">The number of bytes in the byte array to use as data.</param>
        /// <returns>The computed transform.</returns>
        public byte[] TransformFinalBlock(byte[] inputBuffer, int inputOffset, int inputCount)
        {
            byte[] returnBytes = new byte[inputCount];
            for (int i = 0; i < inputCount; i++)
            {
                byte mask = (byte)((_count++ % 255) + 1);
                byte input = (inputBuffer[i + inputOffset]);
                returnBytes[i] = ((mask & input) != 0) ? (byte)(input ^ mask) : (byte)(input | mask);
            }
            _count = 0;
            return returnBytes;
        }

        #endregion

        #region IDisposable Members

        /// <summary>
        /// Performs application-defined tasks associated with freeing, releasing, or resetting unmanaged resources.
        /// </summary>
        public void Dispose()
        {
        }

        #endregion
    }
}
