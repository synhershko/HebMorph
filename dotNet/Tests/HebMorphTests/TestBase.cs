namespace HebMorph.Tests
{
	public abstract class TestBase
	{
		protected static string hspellPath;

		protected TestBase()
		{
			string path = System.IO.Path.GetDirectoryName(this.GetType().Assembly.Location);
			int loc = path.LastIndexOf(System.IO.Path.DirectorySeparatorChar + "dotNet" + System.IO.Path.DirectorySeparatorChar);
			if (loc > -1)
			{
				path = path.Remove(loc + 1);
				hspellPath = System.IO.Path.Combine(path, "hspell-data-files" + System.IO.Path.DirectorySeparatorChar);
			}
		}
	}
}
