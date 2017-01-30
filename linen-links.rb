
Flow = Struct.new :src, :dst

create_symbolic_link = -> flow do
  if File.exist? flow.dst
    puts "already exists : #{flow.dst}"
  else
    puts line = "ln -s #{flow.src} #{flow.dst}"
    `#{line}`
  end
end

def create_directory path
  puts line = "mkdir -p #{path}"
  `#{line}`
end

def get_dst_path
  pwd = Dir::pwd
  basename = File.basename pwd
  parent_dir = File.dirname pwd
  "#{parent_dir}/#{basename}-for-idea"
end

def create_flow_factory dir_flow
  -> name {
    on = -> dir { "#{dir}/#{name}" }
    Flow.new on[dir_flow.src], on[dir_flow.dst]
  }
end

def escape path
  require 'shellwords'
  path.shellescape
end

targets = [
  "android-jars",
  "wheat-modern",
  "wheat-macros",
  "wheat-macros-sample",
  "wheat-ancient",
  "wheat-lore",
  "wheat-calendar",
  "linen-glue",
  "linen-repository",
  "linen-scene",
  "linen-modern",
  "linen-pickle",
  "linen-links.rb",
  "project",
  "local.properties",
  "targets.gradle",
  "build.sbt",
  "build.gradle",
  "README.md",
]

begin
  dst_dir = escape get_dst_path
  to_flow = create_flow_factory Flow.new(escape(Dir::pwd), dst_dir)

  create_directory dst_dir
  targets.map(&to_flow).each &create_symbolic_link
end

