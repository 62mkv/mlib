using System;

namespace DevTreeConverter
{
    class Program
    {
        static void Main(string[] args)
        {
            //Parse Arguments
            CommandLineArguments commandLineArgs = new CommandLineArguments(args);

            //Show Help
            if (commandLineArgs.ContainsArgument("h") ||
                commandLineArgs.ContainsArgument("?"))
            {
                ShowHelp();
                return;
            }

            Console.WriteLine("Development Tree Converter\r\n\r\n");

            //Get Arguments
            BuildArguments buildArguments = GetBuildArguments(commandLineArgs);


            //Check Arguments
            try
            {
                CheckArguments(buildArguments);
            }
            catch (ArgumentException ex)
            {
                Console.WriteLine("Error: {0}", ex.Message);
                return;
            }

            Console.WriteLine("\r\nProduct Name: {0}", buildArguments.ProductName);
            Console.WriteLine("Directory Alias: {0}", buildArguments.DirectoryAlias);
            Console.WriteLine("Product Root Dir: {0}", buildArguments.ProductRootPath);
            Console.WriteLine("Remove Makefiles: {0}", buildArguments.RemoveMakeFiles ? "Yes" : "No");
            Console.WriteLine("Convert Only Mast Makefile: {0}", buildArguments.MasterFileOnly ? "Yes" : "No");
            Console.WriteLine("Create Master Build Files: {0}", buildArguments.CreateBuildFiles ? "Yes" : "No");
            Console.WriteLine("\r\nBeginning Conversion...\r\n");
 
            //Convert the Project
            ProjectConverter converter = new ProjectConverter(buildArguments);
            converter.ConvertProject();

            Console.WriteLine("Conversion Complete");
        }

        private static void CheckArguments(BuildArguments buildArguments)
        {
            if (buildArguments.NoPromptMode)
            {
                if (String.IsNullOrEmpty(buildArguments.ProductName))
                    throw new ArgumentException("Product Name should be defined with the /name switch");

                if (!buildArguments.IsDirectoryAliasValid())
                    throw new ArgumentException("Product Alias is not defined or is invalid. Use the /alias switch");

                if (String.IsNullOrEmpty(buildArguments.ProductRootPath))
                    buildArguments.ProductRootPath = buildArguments.DirectoryAliasPath;

                if (!buildArguments.IsProductRootValid())
                    throw new ArgumentException("Product Start Directory is invalid.");

                return;
            }


            while (String.IsNullOrEmpty(buildArguments.ProductName))
            {
                buildArguments.ProductName =
                    GetInput("Product Name",
                             "Please enter the name of the product as used by comp_ver (MOCA, MCS, SAL etc.)");

            }

            while (String.IsNullOrEmpty(buildArguments.DirectoryAlias))
            {
                buildArguments.DirectoryAlias =
                    GetInput("Product Root Alias",
                             "Please enter The alias used by MOCA to determine the product root. (MOCADIR, MCSDIR, SALDIR etc.)");
            }

            if (buildArguments.IsDirectoryAliasValid())
            {
                Console.WriteLine("\r\nRoot path resolves to: '{0}'\r\n", buildArguments.DirectoryAliasPath);
                buildArguments.ProductRootPath = buildArguments.DirectoryAliasPath;
            }
            bool pathConfirmed = false;

            while (!pathConfirmed || 
                   (!buildArguments.IsProductRootValid()))
            {
                string input = 
                    GetInput("Product Root Path",
                             string.Format("Please enter root direcotry path of the product.\r\nPress 'Enter' to set it to '{0}'", buildArguments.ProductRootPath));

                if (!String.IsNullOrEmpty(input))
                    buildArguments.DirectoryAlias = input;

                pathConfirmed = true;
            }

            //Check makefiles
            string remove = GetInput("Remove (Press 'y' to remove files)", "Do you want to remove the old makefile.nt files?");


            if (!String.IsNullOrEmpty(remove) && remove.Equals("y", StringComparison.InvariantCultureIgnoreCase))
                buildArguments.RemoveMakeFiles = true;

        }

        /// <summary>
        /// Gets the build arguments.
        /// </summary>
        /// <param name="args">The args from the command line after parsing.</param>
        /// <returns>A <see cref="BuildArguments"/> class containing options for construction.</returns>
        private static BuildArguments GetBuildArguments(CommandLineArguments args)
        {
            BuildArguments buildArguments = new BuildArguments();

            if (args.ContainsArgument("n"))
                buildArguments.ProductName = args.GetArgument("n");

            if (args.ContainsArgument("a"))
                buildArguments.DirectoryAlias = args.GetArgument("a");

            if (args.ContainsArgument("s"))
                buildArguments.ProductRootPath = args.GetArgument("s");

            if (args.ContainsArgument("r"))
                buildArguments.RemoveMakeFiles = true;

            if (args.ContainsArgument("q"))
                buildArguments.NoPromptMode = true;

            if (args.ContainsArgument("m"))
                buildArguments.MasterFileOnly = true;

            if (args.ContainsArgument("b"))
                buildArguments.CreateBuildFiles = false;

            return buildArguments;
        }

        /// <summary>
        /// Gets the input from the console.
        /// </summary>
        /// <param name="promptName">Name of the prompt.</param>
        /// <param name="promptDescription">The prompt description.</param>
        /// <returns></returns>
        private static string GetInput(string promptName, string promptDescription)
        {
            Console.WriteLine("\r\n{0}", promptDescription);
            Console.Write("{0}: ", promptName);
            return Console.ReadLine();
        }

        /// <summary>
        /// Shows the help.
        /// </summary>
        private static void ShowHelp()
        {
            Console.WriteLine("MSBuild Tree Conversion Utility");
            Console.WriteLine("Copyright 2008 RedPrairie Corporation\r\n");
            Console.WriteLine("Usage: DevTreeConverter.exe [/n <productName>] [/a <productAlias>] [/s <path>] [/r] [/m] [/b] [/q] [/h] \r\n");
            Console.WriteLine("Arguments:");
            Console.WriteLine("[/n <productName>]    The name of the product as used for comp_ver.");
            Console.WriteLine("                      i.e. MOCA, MCS, SAL,etc.");
            Console.WriteLine("[/a <productAlias>]   The alias used by MOCA to determine the product root.");
            Console.WriteLine("                      i.e. MOCADIR, MCSDIR, SALDIR etc.");
            Console.WriteLine("[/s <path>]           The folder to begin the conversion in.");
            Console.WriteLine("                      If used, and doesn't match the alias path a minor conversion is assumed");
            Console.WriteLine("[/r]                  Removes the existing makefiles from the structure");
            Console.WriteLine("[/m]                  Only converts the master makefile");
            Console.WriteLine("[/b]                  Prevents the creation of the buildfiles items.");
            Console.WriteLine("[/q]                  Runs in a non-interactive mode. Errors will occur if args are missing");
            Console.WriteLine("[/h or /?]            Shows this screen");
        }
    }
}
