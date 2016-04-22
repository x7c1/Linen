
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

targets = [
  "wheat-build",
  "wheat-modern",
  "wheat-macros",
  "wheat-ancient",
  "wheat-lore",
  "linen-glue",
  "linen-repository",
  "linen-scene",
  "linen-modern",
  "linen-pickle",
  "project",
  "local.properties",
]

begin
  dst_dir = get_dst_path
  to_flow = create_flow_factory Flow.new(Dir::pwd, dst_dir)

  create_directory dst_dir
  targets.map(&to_flow).each &create_symbolic_link
end

