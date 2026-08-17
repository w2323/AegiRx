import os
import re
import json

def analyze_java_files(root_dir):
    results = {
        'inheritance': [],
        'composition_aggregation': [],
        'grasp': []
    }
    
    seen_grasp = set()
    
    for subdir, _, files in os.walk(root_dir):
        for file in files:
            if file.endswith('.java'):
                path = os.path.join(subdir, file)
                try:
                    with open(path, 'r', encoding='utf-8') as f:
                        lines = f.readlines()
                        
                    class_name = ""
                    for i, line in enumerate(lines):
                        line_num = i + 1
                        
                        # Inheritance
                        match = re.search(r'class\s+(\w+)\s+(extends|implements)\s+([\w\s,<>]+)', line)
                        if match:
                            class_name = match.group(1)
                            results['inheritance'].append({
                                'file': path,
                                'class': class_name,
                                'type': match.group(2),
                                'parent': match.group(3).strip().replace('{','').strip(),
                                'line': line_num
                            })
                            
                        # Keep track of class name
                        match_class = re.search(r'class\s+(\w+)', line)
                        if match_class and not class_name:
                            class_name = match_class.group(1)
                            
                        # Composition/Aggregation (Fields that are other domain objects or collections)
                        match_field = re.search(r'private\s+(List<[\w]+>|Set<[\w]+>|Map<[\w,\s]+>|[A-Z]\w+)\s+(\w+);', line)
                        if match_field and not 'String' in match_field.group(1) and not 'LocalDate' in match_field.group(1) and not 'Double' in match_field.group(1) and not 'Integer' in match_field.group(1):
                            field_type = match_field.group(1)
                            field_name = match_field.group(2)
                            if field_type not in ['int', 'double', 'float', 'long', 'boolean']:
                                results['composition_aggregation'].append({
                                    'file': path,
                                    'class': class_name,
                                    'field': field_name,
                                    'type': field_type,
                                    'line': line_num
                                })
                                
                    if class_name:
                        # GRASP Patterns
                        # Controller
                        if 'Controller' in class_name:
                            key = f"{class_name}-Controller"
                            if key not in seen_grasp:
                                seen_grasp.add(key)
                                results['grasp'].append({
                                    'file': path,
                                    'class': class_name,
                                    'pattern': 'Controller',
                                    'line': 1,
                                    'explanation': 'Handles UI events and delegates work to services.'
                                })
                                
                        # Creator (Factory)
                        if 'Factory' in class_name or 'Generator' in class_name:
                            key = f"{class_name}-Creator"
                            if key not in seen_grasp:
                                seen_grasp.add(key)
                                results['grasp'].append({
                                    'file': path,
                                    'class': class_name,
                                    'pattern': 'Creator',
                                    'line': 1,
                                    'explanation': 'Responsible for creating objects (e.g. Factory pattern).'
                                })
                                
                        # Information Expert / High Cohesion (DAO, Service)
                        if 'DAO' in class_name or 'Service' in class_name:
                            key = f"{class_name}-InformationExpert"
                            if key not in seen_grasp:
                                seen_grasp.add(key)
                                results['grasp'].append({
                                    'file': path,
                                    'class': class_name,
                                    'pattern': 'Information Expert / High Cohesion',
                                    'line': 1,
                                    'explanation': 'Manages specific domain data operations or business logic, exhibiting high cohesion.'
                                })

                        # Indirection / Polymorphism (Interfaces, Service Locator, etc.)
                        if 'Observer' in class_name:
                            key = f"{class_name}-Polymorphism"
                            if key not in seen_grasp:
                                seen_grasp.add(key)
                                results['grasp'].append({
                                    'file': path,
                                    'class': class_name,
                                    'pattern': 'Polymorphism',
                                    'line': 1,
                                    'explanation': 'Uses polymorphism for event handling or observation.'
                                })
                except Exception as e:
                    pass
                    
    with open('analysis_output.json', 'w', encoding='utf-8') as f:
        json.dump(results, f, indent=2)

analyze_java_files('d:\\UND\\src')
